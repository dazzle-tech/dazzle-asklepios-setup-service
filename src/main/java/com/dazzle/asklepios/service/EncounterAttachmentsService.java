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

    public EncounterAttachments upload(Long encounterId, UploadEncounterAttachmentVM vm) {
        LOG.debug("upload encounter attachment {}", vm);

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
        String key = "encounters/" + encounterId + "/" + YYYY.format(now) + "/" + MM.format(now) + "/" + safeFileName;

        try {
            LOG.debug("store encounter attachment to spaces");
            storage.put(key, mime, size, f.getInputStream());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed", e);
        }

        EncounterAttachments entity = EncounterAttachments.builder()
                .encounterId(encounterId)
                .spaceKey(key)
                .filename(originalName)
                .mimeType(mime)
                .sizeBytes(size)
                .type(vm.type())
                .details(vm.details())
                .source(vm.source())
                .sourceId(vm.sourceId())
                .build();

        return repo.save(entity);
    }
    private static String getOriginalName(MultipartFile f) {
        String name = f.getOriginalFilename();
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
        EncounterAttachments a = repo.findByIdAndDeletedAtIsNull(id).orElseThrow();
        PresignedGetObjectRequest get = storage.presignGet(a.getSpaceKey(), a.getFilename());
        return new DownloadEncounterAttachmentVM(get.url().toString(), props.getPresignExpirySeconds());
    }

    @Transactional
    public void softDelete(Long id) {
        LOG.debug("Soft deleting encounter attachment {}", id);
        int updated = repo.softDelete(id);
        if (updated==0) {
            throw new NotFoundAlertException("Encounter attachment not found with id {" + id+"}", ENTITY_NAME, "notfound");
        }
    }
}
