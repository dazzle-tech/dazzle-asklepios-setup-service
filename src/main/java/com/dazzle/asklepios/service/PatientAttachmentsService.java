package com.dazzle.asklepios.service;

import com.dazzle.asklepios.attachments.AttachmentProperties;
import com.dazzle.asklepios.domain.PatientAttachments;
import com.dazzle.asklepios.repository.PatientAttachmentsRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PatientAttachmentsService {

    private final PatientAttachmentsRepository repo;
    private final AttachmentProperties props;
    private final AttachmentStorageService storage;

    private static final DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("yyyy").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter MM   = DateTimeFormatter.ofPattern("MM").withZone(ZoneOffset.UTC);

    public record UploadTicket(String objectKey, String putUrl) {}
    public record DownloadTicket(String url, int expiresInSeconds) {}

    /** Step 1: Create presigned PUT. Nothing saved to DB here. */
    public UploadTicket presignUpload(Long patientId, String filename, String mime, long size) {
        validateTypeAndSize(mime, size);

        Instant now = Instant.now();
        String safe = filename.replaceAll("[^\\w.\\- ]", "_");
        String key  = "patients/" + patientId + "/" + YYYY.format(now) + "/" + MM.format(now) + "/" + safe;

        PresignedPutObjectRequest put = storage.presignPut(key, mime, size);
        return new UploadTicket(key, put.url().toString());
    }

    @Transactional
    public PatientAttachments finalizeUpload(Long patientId, String objectKey, String createdBy) {
        return finalizeUpload(patientId, objectKey, createdBy, null, null);
    }

    /** Step 2: Finalize. Verify object exists, then insert row with type/details. */
    @Transactional
    public PatientAttachments finalizeUpload(Long patientId, String objectKey, String createdBy, String type, String details) {
        HeadObjectResponse head = storage.head(objectKey);
        String mime = head.contentType();
        Long size   = head.contentLength();

        if (mime == null || size == null) throw new IllegalStateException("object_missing_metadata");
        validateTypeAndSize(mime, size);

        String filename = objectKey.substring(objectKey.lastIndexOf('/') + 1);

        if (repo.existsByPatientIdAndSpaceKey(patientId, objectKey)) {
            return repo.findByPatientIdAndDeletedAtIsNullOrderByCreatedAtDesc(patientId, Pageable.ofSize(1))
                    .stream().filter(a -> a.getSpaceKey().equals(objectKey)).findFirst()
                    .orElseThrow();
        }

        PatientAttachments a = PatientAttachments.builder()
                .id(null)
                .patientId(patientId)
                .createdBy(createdBy)
                .spaceKey(objectKey)
                .filename(filename)
                .mimeType(mime)
                .sizeBytes(size)
                .type(type)
                .details(details)
                .createdAt(Instant.now())
                .build();

        return repo.save(a);
    }

    public Page<PatientAttachments> list(Long patientId, Pageable pageable) {
        return repo.findByPatientIdAndDeletedAtIsNullOrderByCreatedAtDesc(patientId, pageable);
    }

    public DownloadTicket downloadUrl(Long id) {
        PatientAttachments a = repo.findActiveById(id).orElseThrow();
        PresignedGetObjectRequest get = storage.presignGet(a.getSpaceKey(), a.getFilename());
        return new DownloadTicket(get.url().toString(), props.getPresignExpirySeconds());
    }

    @Transactional
    public void softDelete(Long id) {
        PatientAttachments a = repo.findById(id).orElseThrow();
        if (a.getDeletedAt() == null) {
            storage.delete(a.getSpaceKey());
            a.setDeletedAt(Instant.now());
            repo.save(a);
        }
    }

    private void validateTypeAndSize(String mime, long size) {
        Set<String> allowed = props.getAllowed();
        if (allowed == null || !allowed.contains(mime))
            throw new BadRequestAlertException("unsupported_type", "PatientAttachments", "unsupportedType");
        if (size > props.getMaxBytes())
            throw new BadRequestAlertException("too_large", "PatientAttachments", "tooLarge");
    }
}
