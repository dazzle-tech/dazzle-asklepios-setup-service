package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.attachments.AttachmentProperties;
import com.dazzle.asklepios.domain.EncounterAttachments;
import com.dazzle.asklepios.domain.enumeration.EncounterAttachmentSource;
import com.dazzle.asklepios.repository.EncounterAttachementsRepository;
import com.dazzle.asklepios.service.AttachmentStorageService;
import com.dazzle.asklepios.service.EncounterAttachmentsService;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/setup")
@Validated
public class EncounterAttachmentsController {
    private final EncounterAttachmentsService service;
    private final EncounterAttachementsRepository repo;
    private final AttachmentStorageService storage;
    private final AttachmentProperties props;

    private static final DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("yyyy").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter MM   = DateTimeFormatter.ofPattern("MM").withZone(ZoneOffset.UTC);

    private static final String ENTITY_NAME = "EncounterAttachments";

    public record UploadResponse( Long id, String filename, String mimeType, long sizeBytes, String type, String details,String downloadUrl) {}
    public record UpdateAttachmentRequest(String type, String details) {}

    public EncounterAttachmentsController(EncounterAttachmentsService service, EncounterAttachementsRepository repo, AttachmentStorageService storage, AttachmentProperties props) {
        this.service = service;
        this.repo = repo;
        this.storage = storage;
        this.props = props;
    }
    /**
     * {@Code: POST/ }
     *  One call: receive file(s) → upload to Spaces → verify → insert row(s) → return presigned download URL(s).

     **/
    @PostMapping(value = "/encounters/{encounterId}/attachments", consumes = "multipart/form-data")
    public List<EncounterAttachmentsController.UploadResponse> upload(@PathVariable Long encounterId, @RequestParam(required = false) String type, @RequestParam(required = false) String details, @RequestParam(required = false) EncounterAttachmentSource source, @RequestPart("files") @NotNull List<MultipartFile> files) {
        Instant now = Instant.now();
        return files.stream().map(f -> {
            String mime = f.getContentType() == null ? "application/octet-stream" : f.getContentType();
            long size = f.getSize();

            if (!props.getAllowed().contains(mime)) {
                throw new BadRequestAlertException("Unsupported file type", ENTITY_NAME, "unsupported_type");
            }
            if (size > props.getMaxBytes()) {
                throw new BadRequestAlertException("File too large", ENTITY_NAME, "too_large");
            }

            String originalName = f.getOriginalFilename() == null
                    ? "file"
                    : f.getOriginalFilename().replaceAll("[^\\w.\\- ]", "_");

            String uniqueId = UUID.randomUUID().toString();
            String safeFileName = uniqueId + "_" + originalName;

            String key = "encounters/" + encounterId + "/" + YYYY.format(now) + "/" + MM.format(now) + "/" + safeFileName;

            try {
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
                    .type(type)
                    .details(details)
                    .source(source)
                    .build();

            entity = repo.save(entity);
            String downloadUrl = storage.presignGet(key, safeFileName).url().toString();

            return new EncounterAttachmentsController.UploadResponse(
                    entity.getId(),
                    originalName,
                    mime,
                    size,
                    entity.getType(),
                    entity.getDetails(),
                    downloadUrl
            );
        }).toList();
    }

}
