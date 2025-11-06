package com.dazzle.asklepios.service;

import com.dazzle.asklepios.attachments.AttachmentProperties;
import com.dazzle.asklepios.domain.EncounterAttachments;
import com.dazzle.asklepios.domain.InventoryTransferAttachments;
import com.dazzle.asklepios.repository.InventoryTransferAttachmentsRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransfer.DownloadInventoryTransferAttachmentVM;
import com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransfer.UploadInventoryTransferAttachmentVM;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryTransferAttachmentsService {

    private final InventoryTransferAttachmentsRepository repo;
    private final AttachmentProperties props;
    private final AttachmentStorageService storage;

    private static final String ENTITY_NAME = "InventoryTransferAttachments";

    private static final DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("yyyy").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter MM   = DateTimeFormatter.ofPattern("MM").withZone(ZoneOffset.UTC);

    private static final Logger LOG = LoggerFactory.getLogger(InventoryTransferAttachmentsService.class);

    public InventoryTransferAttachments upload(Long transactionId, UploadInventoryTransferAttachmentVM uploadInventoryTransferAttachmentVM) {
        LOG.debug("upload Inventory Transfer attachment {}", uploadInventoryTransferAttachmentVM);

        MultipartFile file = uploadInventoryTransferAttachmentVM.file();
        if (file == null || file.isEmpty()) {
            throw new BadRequestAlertException("No file provided", ENTITY_NAME, "no_file");
        }

        Instant now = Instant.now();
        String mime = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        long size = file.getSize();

        if (!props.getAllowed().contains(mime)) {
            throw new BadRequestAlertException("Unsupported file type", ENTITY_NAME, "unsupported_type");
        }
        if (size > props.getMaxBytes()) {
            throw new BadRequestAlertException("File too large", ENTITY_NAME, "too_large");
        }

        String originalName = getOriginalName(file);
        String safeFileName = UUID.randomUUID() + "_" + originalName;
        String key = "inventoryTransfer/" + transactionId + "/" + YYYY.format(now) + "/" + MM.format(now) + "/" + safeFileName;

        try {
            LOG.debug("store Inventory Transfer attachment to spaces");
            storage.put(key, mime, size, file.getInputStream());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed", e);
        }

        InventoryTransferAttachments inventoryTransferAttachments = InventoryTransferAttachments.builder()
                .transactionId(transactionId)
                .spaceKey(key)
                .filename(originalName)
                .mimeType(mime)
                .sizeBytes(size)
                .build();

        return repo.save(inventoryTransferAttachments);
    }
    private static String getOriginalName(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || name.isBlank()) return "file";
        return name.replaceAll("[^\\w.\\- ]", "_");
    }

    public List<InventoryTransferAttachments> list(Long transactionId) {
     LOG.debug("list Inventory Transfer attachments {}", transactionId);
        return repo.findByTransactionIdAndDeletedAtIsNullOrderByCreatedDateDesc(transactionId);
    }

    public DownloadInventoryTransferAttachmentVM downloadUrl(Long id) {
        LOG.debug("download Inventory Transaction attachments{}", id);
        InventoryTransferAttachments inventoryTransferAttachments = repo.findByIdAndDeletedAtIsNull(id).orElseThrow();
        PresignedGetObjectRequest getURL = storage.presignGet(inventoryTransferAttachments.getSpaceKey(), inventoryTransferAttachments.getFilename());
        return new DownloadInventoryTransferAttachmentVM(getURL.url().toString(), props.getPresignExpirySeconds());
    }

    @Transactional
    public void softDelete(Long id) {
        LOG.debug("delete inventory transfer attachments {}", id);
        InventoryTransferAttachments a = repo.findById(id).orElseThrow(()-> new NotFoundAlertException(" Inventory Transfer attachment not found  id: "+id, ENTITY_NAME,"notfound"));
        if (a.getDeletedAt() == null) {
            a.setDeletedAt(Instant.now());
            repo.save(a);
        }
    }
}
