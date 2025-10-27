// PatientAttachmentController.java
package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.attachments.AttachmentProperties;
import com.dazzle.asklepios.domain.PatientAttachments;
import com.dazzle.asklepios.domain.enumeration.PatientAttachmentSource;
import com.dazzle.asklepios.repository.PatientAttachmentsRepository;
import com.dazzle.asklepios.service.AttachmentStorageService;
import com.dazzle.asklepios.service.PatientAttachmentsService;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.validation.constraints.NotNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
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
public class PatientAttachmentsController {
    private final PatientAttachmentsService service;
    private final PatientAttachmentsRepository repo;
    private final AttachmentStorageService storage;
    private final AttachmentProperties props;

    private static final DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("yyyy").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter MM = DateTimeFormatter.ofPattern("MM").withZone(ZoneOffset.UTC);

    private static final String ENTITY_NAME = "patientAttachments";

    public record UploadResponse(Long id, String filename, String mimeType, long sizeBytes, String type, String details,
                                 String downloadUrl) {
    }

    public record UpdateAttachmentRequest(String type, String details) {
    }

    public PatientAttachmentsController(PatientAttachmentsRepository repo, AttachmentStorageService storage, AttachmentProperties props, PatientAttachmentsService service) {
        this.repo = repo;
        this.storage = storage;
        this.props = props;
        this.service = service;
    }

    /**
     * One call: receive file(s) → upload to Spaces → verify → insert row(s) → return presigned download URL(s).
     */
    @PostMapping(value = "/patients/{patientId}/attachments", consumes = "multipart/form-data")
    public List<UploadResponse> upload(@PathVariable Long patientId, @RequestParam(required = false) String type, @RequestParam(required = false) String details, @RequestParam(required = false) PatientAttachmentSource source, @RequestPart("files") @NotNull List<MultipartFile> files) {
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

            String key = "patients/" + patientId + "/" + YYYY.format(now) + "/" + MM.format(now) + "/" + safeFileName;

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

            PatientAttachments entity = PatientAttachments.builder()
                    .patientId(patientId)
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

            return new UploadResponse(
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

    /**
     * List active attachments for a patient.
     */
    @GetMapping("/patients/attachments-list-by-patientId/{patientId}")
    public List<PatientAttachments> list(@PathVariable Long patientId) {
        return service.list(patientId);
    }

    /**
     * Presigned download URL.
     */
    @PostMapping("/patients/attachmentDownloadUrl/{id}")
    public PatientAttachmentsService.DownloadTicket downloadUrl(@PathVariable Long id) {
        PatientAttachmentsService.DownloadTicket downloadTicket = service.downloadUrl(id);
        return new PatientAttachmentsService.DownloadTicket(downloadTicket.url(), downloadTicket.expiresInSeconds());
    }

    /**
     * Soft delete
     */
    @DeleteMapping("/patients/attachments/{id}")
    public void delete(@PathVariable Long id) {
        service.softDelete(id);
    }

    /**
     * update type/details for attachments
     **/
    @PutMapping("/patients/attachments/{id}")
    public PatientAttachments updateTypeAndDetails(@PathVariable Long id, @RequestBody UpdateAttachmentRequest updateAttachmentRequest) {
        PatientAttachments patientAttachments = repo.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Not found", ENTITY_NAME, "not_found"));

        if (updateAttachmentRequest.type() != null) patientAttachments.setType(updateAttachmentRequest.type());
        if (updateAttachmentRequest.details() != null) patientAttachments.setDetails(updateAttachmentRequest.details());

        return repo.save(patientAttachments);
    }

    @GetMapping("/patients/{patientId}/profile-picture")
    @ResponseStatus(HttpStatus.OK)
    public PatientAttachmentsService.DownloadTicket getLatestProfilePicture(@PathVariable Long patientId) {
        PatientAttachments patientAttachments = repo
                .findFirstByPatientIdAndSourceAndDeletedAtIsNullOrderByCreatedDateDesc(
                        patientId, PatientAttachmentSource.PATIENT_PROFILE_PICTURE
                ).orElseThrow(() -> new BadRequestAlertException("No profile picture", ENTITY_NAME, "not_found"));
        PatientAttachmentsService.DownloadTicket downloadTicket = service.downloadUrl(patientAttachments.getId());
        return new PatientAttachmentsService.DownloadTicket(downloadTicket.url(), downloadTicket.expiresInSeconds());
    }
    /**
     * {@code: GET /patients/attachments-list-by-PatientIdAndEncounterId/{patientId}}: List active attachments for patient and related encounter .}
     * recieve all files related to the patient's encounters and patient
     * @param encounterId  to get attachments by list of encounters
     * @param patientId to get attachments by patientId
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}, a list of patientAttachment view models in the body,
     * @apiNote /api/setup/patients/123/attachments?encounterId=1,2,3 or /api/setup/patients/123/attachments?encounterId=1&encounterId=2&encounterId=3
     **/
    @GetMapping("/patients/attachments-list-by-PatientIdAndEncounterId/{patientId}")
    public List<PatientAttachments> listPatientAttachments(@PathVariable Long patientId, @RequestParam(required = false) List<Long> encounterId) {

        //LOG.debug("Listing attachments for patientId={} and encounterIds={}", patientId, encounterId);
        return service.list(patientId, encounterId);
    }
}
