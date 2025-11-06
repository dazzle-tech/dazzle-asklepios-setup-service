package com.dazzle.asklepios.service;

import com.dazzle.asklepios.attachments.AttachmentProperties;
import com.dazzle.asklepios.domain.EncounterAttachments;
import com.dazzle.asklepios.domain.enumeration.EncounterAttachmentSource;
import com.dazzle.asklepios.repository.EncounterAttachementsRepository;
import com.dazzle.asklepios.web.rest.DepartmentController;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.attachment.encounter.DownloadEncounterAttachmentVM;
import com.dazzle.asklepios.web.rest.vm.attachment.encounter.UploadEncounterAttachmentVM;
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
public class EncounterAttachmentsService {

    private final EncounterAttachementsRepository repo;
    private final AttachmentProperties props;
    private final AttachmentStorageService storage;

    private static final String ENTITY_NAME = "EncounterAttachments";

    private static final DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("yyyy").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter MM   = DateTimeFormatter.ofPattern("MM").withZone(ZoneOffset.UTC);

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentController.class);

    public EncounterAttachments upload(Long encounterId, UploadEncounterAttachmentVM uploadEncounterAttachmentVM) {
        LOG.debug("upload encounter attachment {}", uploadEncounterAttachmentVM);

        MultipartFile file = uploadEncounterAttachmentVM.file();
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
        String key = "encounters/" + encounterId + "/" + YYYY.format(now) + "/" + MM.format(now) + "/" + safeFileName;

        try {
            LOG.debug("store encounter attachment to spaces");
            storage.put(key, mime, size, file.getInputStream());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed", e);
        }

        EncounterAttachments encounterAttachments = EncounterAttachments.builder()
                .encounterId(encounterId)
                .spaceKey(key)
                .filename(originalName)
                .mimeType(mime)
                .sizeBytes(size)
                .type(uploadEncounterAttachmentVM.type())
                .details(uploadEncounterAttachmentVM.details())
                .source(uploadEncounterAttachmentVM.source())
                .sourceId(uploadEncounterAttachmentVM.sourceId())
                .build();

        return repo.save(encounterAttachments);
    }
    private static String getOriginalName(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || name.isBlank()) return "file";
        return name.replaceAll("[^\\w.\\- ]", "_");
    }

    public List<EncounterAttachments> list(List<Long> encounterId) {
     LOG.debug("list encounter attachments {}", encounterId);
        return repo.findByEncounterIdInAndDeletedAtIsNullOrderByCreatedDateDesc(encounterId);
    }
    public List<EncounterAttachments> listByEncounterIdAndSource(Long encounterId, EncounterAttachmentSource source, Long sourceId) {
        if (sourceId == null) {
            return repo.findByEncounterIdAndSourceAndDeletedAtIsNullOrderByCreatedDateDesc(encounterId, source);
        }
        return repo.findByEncounterIdAndSourceAndSourceIdAndDeletedAtIsNullOrderByCreatedDateDesc(encounterId, source, sourceId);
    }


    public DownloadEncounterAttachmentVM downloadUrl(Long id) {
        LOG.debug("download encounter attachments {}", id);
        EncounterAttachments encounterAttachments = repo.findByIdAndDeletedAtIsNull(id).orElseThrow();
        PresignedGetObjectRequest getURL = storage.presignGet(encounterAttachments.getSpaceKey(), encounterAttachments.getFilename());
        return new DownloadEncounterAttachmentVM(getURL.url().toString(), props.getPresignExpirySeconds());
    }

    @Transactional
    public void softDelete(Long id) {
        LOG.debug("delete encounter attachments {}", id);
        EncounterAttachments a = repo.findById(id).orElseThrow(()-> new NotFoundAlertException(" Encounter attachment not found  id: "+id, ENTITY_NAME,"notfound"));
        if (a.getDeletedAt() == null) {
            a.setDeletedAt(Instant.now());
            repo.save(a);
        }
    }
}
