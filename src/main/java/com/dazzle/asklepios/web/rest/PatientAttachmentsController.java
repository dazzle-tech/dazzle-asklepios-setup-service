// PatientAttachmentController.java
package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.attachments.AttachmentProperties;
import com.dazzle.asklepios.domain.PatientAttachments;
import com.dazzle.asklepios.repository.PatientAttachmentsRepository;
import com.dazzle.asklepios.service.AttachmentStorageService;
import com.dazzle.asklepios.service.PatientAttachmentsService;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/setup/patients")
@Validated
public class PatientAttachmentsController {
    private final PatientAttachmentsService service;
    private final PatientAttachmentsRepository repo;
    private final AttachmentStorageService storage;
    private final AttachmentProperties props;

    private static final DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("yyyy").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter MM   = DateTimeFormatter.ofPattern("MM").withZone(ZoneOffset.UTC);

    public record UploadResponse(Long id, String filename, String mimeType, long sizeBytes, String downloadUrl) {}

    public PatientAttachmentsController(PatientAttachmentsRepository repo,
                                       AttachmentStorageService storage,
                                       AttachmentProperties props, PatientAttachmentsService service) {
        this.repo = repo; this.storage = storage; this.props = props;
        this.service = service;
    }

    /** One call: receive file(s) → upload to Spaces → verify → insert row(s) → return presigned download URL(s). */
    @PostMapping(value = "{patientId}/attachments", consumes = "multipart/form-data")
    public List<UploadResponse> upload(
            @PathVariable Long patientId,
            @RequestParam @NotBlank String createdBy,
            @RequestPart("files") @NotNull List<MultipartFile> files
    ) {
        var now = Instant.now();
        return files.stream().map(f -> {
            String mime = f.getContentType() == null ? "application/octet-stream" : f.getContentType();
            long size = f.getSize();
            if (!props.getAllowed().contains(mime)) throw new IllegalArgumentException("unsupported_type");
            if (size > props.getMaxBytes()) throw new IllegalArgumentException("too_large");

            String safe = f.getOriginalFilename() == null ? "file" : f.getOriginalFilename().replaceAll("[^\\w.\\- ]", "_");
            String key  = "patients/" + patientId + "/" + YYYY.format(now) + "/" + MM.format(now) + "/" + safe;

            try {
                storage.put(key, mime, size, f.getInputStream());
            } catch (Exception e) {
                throw new RuntimeException("upload_failed", e);
            }

            var head = storage.head(key);
            if (head.contentLength() == null || head.contentLength() != size) throw new IllegalStateException("size_mismatch");
            if (head.contentType() == null || !head.contentType().equals(mime)) throw new IllegalStateException("type_mismatch");

            var entity = PatientAttachments.builder()
                    .patientId(patientId)
                    .createdBy(createdBy)
                    .spaceKey(key)
                    .filename(safe)
                    .mimeType(mime)
                    .sizeBytes(size)
                    .createdAt(now)
                    .build();
            entity = repo.save(entity);

            var get = storage.presignGet(key, safe);
            return new UploadResponse(entity.getId(), safe, mime, size, get.url().toString());
        }).toList();
    }


    /** List active attachments for a patient. */
    @GetMapping("{patientId}/attachments")
    public Page<PatientAttachments> list(@PathVariable Long patientId, Pageable pageable) {
        return service.list(patientId, pageable);
    }

    /** Presigned download URL. */
    @PostMapping("attachments/{id}:downloadUrl")
    public PatientAttachmentsService.DownloadTicket downloadUrl(@PathVariable Long id) {
        var t = service.downloadUrl(id);
        return new PatientAttachmentsService.DownloadTicket(t.url(), t.expiresInSeconds());
    }

    /** Soft delete + remove from Spaces. */
    @DeleteMapping("attachments/{id}")
    public void delete(@PathVariable Long id) {
        service.softDelete(id);
    }
}
