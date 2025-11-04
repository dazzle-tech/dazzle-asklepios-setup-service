package com.dazzle.asklepios.service;

import com.dazzle.asklepios.attachments.AttachmentProperties;
import com.dazzle.asklepios.domain.PatientAttachments;
import com.dazzle.asklepios.repository.PatientAttachmentsRepository;
import com.dazzle.asklepios.web.rest.DepartmentController;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.attachment.patient.UploadPatientAttachmentVM;
import com.dazzle.asklepios.web.rest.vm.attachment.patient.DownloadPatientAttachmentVM; // <-- add

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
public class PatientAttachmentsService {

    private final PatientAttachmentsRepository repo;
    private final AttachmentProperties props;
    private final AttachmentStorageService storage;

    private static final String ENTITY_NAME = "PatientAttachments";

    private static final DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("yyyy").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter MM = DateTimeFormatter.ofPattern("MM").withZone(ZoneOffset.UTC);

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentController.class);

    public PatientAttachments upload(Long patientId, UploadPatientAttachmentVM vm) {
        LOG.debug("upload patient attachment {}", vm);
        MultipartFile f = vm.file();
        if (f == null || f.isEmpty()) {
            throw new BadRequestAlertException("No file provided", ENTITY_NAME, "no_file");
        }
        Instant now = Instant.now();
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
        String key = "patients/" + patientId + "/" + YYYY.format(now) + "/" + MM.format(now) + "/" + safeFileName;

        try {
            LOG.debug("store patient attachment to spaces");
            storage.put(key, mime, size, f.getInputStream());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed", e);
        }

        PatientAttachments entity = new PatientAttachments().builder()
                .patientId(patientId)
                .spaceKey(key)
                .filename(originalName)
                .mimeType(mime)
                .sizeBytes(size)
                .type(vm.type())
                .details(vm.details())
                .source(vm.source())
                .build();

        return repo.save(entity);
    }

    private static String getOriginalName(MultipartFile f) {
        String name = f.getOriginalFilename();
        if (name == null || name.isBlank()) return "file";
        return name.replaceAll("[^\\w.\\- ]", "_");
    }

    public List<PatientAttachments> list(Long patientId) {
        LOG.debug("list patient attachments {}", patientId);
        return repo.findByPatientIdAndDeletedAtIsNullOrderByCreatedDateDesc(patientId);
    }

    public DownloadPatientAttachmentVM downloadUrl(Long id) {
        LOG.debug("download patient attachments {}", id);
        PatientAttachments pa = repo.findByIdAndDeletedAtIsNull(id).orElseThrow();
        PresignedGetObjectRequest get = storage.presignGet(pa.getSpaceKey(), pa.getFilename());
        return new DownloadPatientAttachmentVM(get.url().toString(), props.getPresignExpirySeconds());
    }

    @Transactional
    public void softDelete(Long id) {
        LOG.debug("Soft deleting patient attachment {}", id);
        int updated = repo.softDelete(id);
        if (updated==0) {
            throw new NotFoundAlertException("Patient attachment not found with id {" + id+"}", ENTITY_NAME, "notfound");
        }
    }
}
