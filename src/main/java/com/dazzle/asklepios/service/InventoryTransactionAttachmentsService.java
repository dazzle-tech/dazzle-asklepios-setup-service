package com.dazzle.asklepios.service;

import com.dazzle.asklepios.attachments.AttachmentProperties;
import com.dazzle.asklepios.domain.InventoryTransactionAttachments;
import com.dazzle.asklepios.repository.InventoryTransactionAttachmentsRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransaction.DownloadInventoryTransactionAttachmentVM;
import com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransaction.UploadInventoryTransactionAttachmentVM;
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
public class InventoryTransactionAttachmentsService {

    private final InventoryTransactionAttachmentsRepository repo;
    private final AttachmentProperties props;
    private final AttachmentStorageService storage;

    private static final String ENTITY_NAME = "InventoryTransactionAttachments";

    private static final DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("yyyy").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter MM   = DateTimeFormatter.ofPattern("MM").withZone(ZoneOffset.UTC);

    private static final Logger LOG = LoggerFactory.getLogger(InventoryTransactionAttachmentsService.class);

    public InventoryTransactionAttachments upload(Long transactionId, UploadInventoryTransactionAttachmentVM vm) {
        LOG.debug("upload Inventory Transaction attachment {}", vm);

        MultipartFile file = vm.file();
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
        String key = "inventoryTransactions/" + transactionId + "/" + YYYY.format(now) + "/" + MM.format(now) + "/" + safeFileName;

        try {
            LOG.debug("store Inventory Transaction attachment to spaces");
            storage.put(key, mime, size, file.getInputStream());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed", e);
        }

        InventoryTransactionAttachments entity = InventoryTransactionAttachments.builder()
                .transactionId(transactionId)
                .spaceKey(key)
                .filename(originalName)
                .mimeType(mime)
                .sizeBytes(size)
                .build();

        return repo.save(entity);
    }
    private static String getOriginalName(MultipartFile f) {
        String name = f.getOriginalFilename();
        if (name == null || name.isBlank()) return "file";
        return name.replaceAll("[^\\w.\\- ]", "_");
    }

    public List<InventoryTransactionAttachments> list(Long transactionId) {
     LOG.debug("list Inventory Transaction attachments {}", transactionId);
        return repo.findByTransactionIdAndDeletedAtIsNullOrderByCreatedDateDesc(transactionId);
    }

    public DownloadInventoryTransactionAttachmentVM downloadUrl(Long id) {
        LOG.debug("download Inventory Transaction attachments{}", id);
        InventoryTransactionAttachments a = repo.findByIdAndDeletedAtIsNull(id).orElseThrow();
        PresignedGetObjectRequest get = storage.presignGet(a.getSpaceKey(), a.getFilename());
        return new DownloadInventoryTransactionAttachmentVM(get.url().toString(), props.getPresignExpirySeconds());
    }

    @Transactional
    public void softDelete(Long id) {
        LOG.debug("delete inventory transaction attachments{}", id);
        int updated = repo.softDelete(id);
        if (updated == 0) {
            throw new NotFoundAlertException("Inventory transaction attachment not found with id {" + id+"}", ENTITY_NAME, "notfound");
        }
    }
}
