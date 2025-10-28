package com.dazzle.asklepios.service;

import com.dazzle.asklepios.attachments.AttachmentProperties;
import com.dazzle.asklepios.domain.EncounterAttachments;
import com.dazzle.asklepios.domain.enumeration.EncounterAttachmentSource;
import com.dazzle.asklepios.repository.EncounterAttachementsRepository;
import com.dazzle.asklepios.web.rest.DepartmentController;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.attachment.encounter.UploadEncounterAttachmentVM;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EncounterAttachmentsService {

    private final EncounterAttachementsRepository repo;
    private final AttachmentProperties props;
    private final AttachmentStorageService storage;

    private static final String ENTITY_NAME = "EncounterAttachments";

    private static final DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("yyyy").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter MM   = DateTimeFormatter.ofPattern("MM").withZone(ZoneOffset.UTC);

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentController.class);

    public record DownloadTicket(String url, int expiresInSeconds) {}
    public List<EncounterAttachments> upload(Long encounterId, UploadEncounterAttachmentVM uploadEncounterAttachmentVM) {
        LOG.debug("upload encounter attachments", uploadEncounterAttachmentVM);
        Instant now = Instant.now();
        return uploadEncounterAttachmentVM.files().stream().map(f -> {
            String mime = f.getContentType() == null ? "application/octet-stream" : f.getContentType();
            long size = f.getSize();

            if (!props.getAllowed().contains(mime)) {
                throw new BadRequestAlertException("Unsupported file type", ENTITY_NAME, "unsupported_type");
            }
            if (size > props.getMaxBytes()) {
                throw new BadRequestAlertException("File too large", ENTITY_NAME, "too_large");
            }

            String originalName = getOriginalName(f);
            String safeFileName = UUID.randomUUID() + "_" + originalName;
            String key = "encounters/" + encounterId + "/" + YYYY.format(now) + "/" + MM.format(now) + "/" + safeFileName;

            try {
                LOG.debug("store encounter attachments to spaces");
                storage.put(key, mime, size, f.getInputStream());
            } catch (Exception e) {
                throw new BadRequestAlertException("Upload failed", ENTITY_NAME, "upload_failed");
            }

            Long contentLength = storage.head(key).contentLength();
            if (contentLength == null || contentLength != size) {
                throw new BadRequestAlertException("Size mismatch", ENTITY_NAME, "size_mismatch");
            }

            String contentType = storage.head(key).contentType();
            if (contentType == null || !contentType.equals(mime)) {
                throw new BadRequestAlertException("Type mismatch", ENTITY_NAME, "type_mismatch");
            }

            EncounterAttachments entity = EncounterAttachments.builder()
                    .encounterId(encounterId)
                    .spaceKey(key)
                    .filename(originalName)
                    .mimeType(mime)
                    .sizeBytes(size)
                    .type(uploadEncounterAttachmentVM.type())
                    .details(uploadEncounterAttachmentVM.details())
                    .source(uploadEncounterAttachmentVM.source())
                    .build();

            return repo.save(entity);
        }).toList();}

    private static String getOriginalName(MultipartFile f) {
        String name = f.getOriginalFilename();
        if (name == null || name.isBlank()) return "file";
        return name.replaceAll("[^\\w.\\- ]", "_");
    }

    public List<EncounterAttachments> list(List<Long> encounterId) {
     LOG.debug("list encounter attachments {}", encounterId);
        return repo.findByEncounterIdInAndDeletedAtIsNullOrderByCreatedDateDesc(encounterId);
    }
    public List<EncounterAttachments> listByEncounterIdAndSource(Long encounterId,EncounterAttachmentSource source) {
        return repo.findByEncounterIdAndSourceAndDeletedAtIsNullOrderByCreatedDateDesc(encounterId,source);
    }

    public DownloadTicket downloadUrl(Long id) {
        LOG.debug("download encounter attachments", id);
        EncounterAttachments a = repo.findActiveById(id).orElseThrow();
        PresignedGetObjectRequest get = storage.presignGet(a.getSpaceKey(), a.getFilename());
        return new DownloadTicket(get.url().toString(), props.getPresignExpirySeconds());
    }

    @Transactional
    public void softDelete(Long id) {
        LOG.debug("delete encounter attachments", id);
        EncounterAttachments a = repo.findById(id).orElseThrow();
        if (a.getDeletedAt() == null) {
            a.setDeletedAt(Instant.now());
            repo.save(a);
        }
    }
}
