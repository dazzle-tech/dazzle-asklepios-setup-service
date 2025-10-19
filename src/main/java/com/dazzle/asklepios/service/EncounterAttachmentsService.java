package com.dazzle.asklepios.service;

import com.dazzle.asklepios.attachments.AttachmentProperties;
import com.dazzle.asklepios.domain.EncounterAttachments;
import com.dazzle.asklepios.repository.EncounterAttachementsRepository;
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

@Service
@RequiredArgsConstructor
public class EncounterAttachmentsService {

    private final EncounterAttachementsRepository repo;
    private final AttachmentProperties props;
    private final AttachmentStorageService storage;

    private static final DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("yyyy").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter MM   = DateTimeFormatter.ofPattern("MM").withZone(ZoneOffset.UTC);

    public record UploadTicket(Long id, String objectKey, String putUrl) {}
    public record DownloadTicket(String url, int expiresInSeconds) {}

    /** Create upload ticket and persist record including source column. */
    public UploadTicket createUpload(Long id, Long encounterId, String filename, String mime, long size, String createdBy, String source) {
        if (!props.getAllowed().contains(mime)) throw new IllegalArgumentException("unsupported_type");
        if (size > props.getMaxBytes()) throw new IllegalArgumentException("too_large");

        Instant now = Instant.now();
        String safe = filename.replaceAll("[^\\w.\\- ]", "_");
        String key = "encounters/" + encounterId + "/" + YYYY.format(now) + "/" + MM.format(now) + "/" + id + "-" + safe;

        EncounterAttachments a = EncounterAttachments.builder()
                .id(id)
                .encounterId(encounterId)
                .createdBy(createdBy)
                .spaceKey(key)
                .filename(filename)
                .mimeType(mime)
                .sizeBytes(size)
                .source(source)        // NEW FIELD
                .createdAt(now)
                .build();

        repo.save(a);

        PresignedPutObjectRequest put = storage.presignPut(key, mime, size);
        return new UploadTicket(id, key, put.url().toString());
    }

    /** Overload for backward compatibility if source is not passed. */
    public UploadTicket createUpload(Long id, Long encounterId, String filename, String mime, long size, String createdBy) {
        return createUpload(id, encounterId, filename, mime, size, createdBy, null);
    }

    @Transactional
    public EncounterAttachments finalizeUpload(Long id) {
        EncounterAttachments a = repo.findById(id).orElseThrow();
        HeadObjectResponse head = storage.head(a.getSpaceKey());
        if (head.contentLength() == null || head.contentLength() != a.getSizeBytes())
            throw new IllegalStateException("size_mismatch");
        if (head.contentType() == null || !head.contentType().equals(a.getMimeType()))
            throw new IllegalStateException("type_mismatch");
        return a;
    }

    public Page<EncounterAttachments> list(Long encounterId, Pageable pageable) {
        return repo.findByEncounterIdAndDeletedAtIsNullOrderByCreatedAtDesc(encounterId, pageable);
    }

    public DownloadTicket downloadUrl(Long id) {
        EncounterAttachments a = repo.findActiveById(id).orElseThrow();
        PresignedGetObjectRequest get = storage.presignGet(a.getSpaceKey(), a.getFilename());
        return new DownloadTicket(get.url().toString(), props.getPresignExpirySeconds());
    }

    @Transactional
    public void softDelete(Long id) {
        EncounterAttachments a = repo.findById(id).orElseThrow();
        if (a.getDeletedAt() == null) {
            storage.delete(a.getSpaceKey());
            a.setDeletedAt(Instant.now());
            repo.save(a);
        }
    }
}
