package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.EncounterAttachments;
import com.dazzle.asklepios.domain.enumeration.EncounterAttachmentSource;
import com.dazzle.asklepios.repository.EncounterAttachementsRepository;
import com.dazzle.asklepios.service.AttachmentStorageService;
import com.dazzle.asklepios.service.EncounterAttachmentsService;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.attachment.encounter.DownloadEncounterAttachmentVM;
import com.dazzle.asklepios.web.rest.vm.attachment.encounter.UpdateEncounterAttachmentVM;
import com.dazzle.asklepios.web.rest.vm.attachment.encounter.UploadEncounterAttachmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.attachment.encounter.UploadEncounterAttachmentVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/setup")
@Validated
public class EncounterAttachmentsController {
    private final EncounterAttachmentsService service;
    private final EncounterAttachementsRepository repo;
    private final AttachmentStorageService storage;

    private static final String ENTITY_NAME = "EncounterAttachments";

    private static final Logger LOG = LoggerFactory.getLogger(EncounterAttachmentsController.class);

    public EncounterAttachmentsController(EncounterAttachmentsService service, EncounterAttachementsRepository repo, AttachmentStorageService storage) {
        this.service = service;
        this.repo = repo;
        this.storage = storage;
    }

    /**
     * {@code : POST/ encounters/{encounterId}/attachments}: Upload new attachment for an encounter
     * One call: receive file(s) → upload to Spaces → verify → insert row(s) → return presigned download URL(s).
     *
     * @param uploadEncounterAttachmentVM the upload payload.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and body of the uploaded attachment,
     * or {@code 400 (Bad Request)} if the payload is invalid (mismatch size and type).
     **/
    @PostMapping(value = "/encounters/{encounterId}/attachments", consumes = "multipart/form-data")
    public ResponseEntity<UploadEncounterAttachmentResponseVM> upload(@PathVariable Long encounterId, @ModelAttribute @Valid UploadEncounterAttachmentVM uploadEncounterAttachmentVM) {
        LOG.debug("Uploading encounter attachment: {}", uploadEncounterAttachmentVM);
        EncounterAttachments saved = service.upload(encounterId, uploadEncounterAttachmentVM);
        String safeFileName = getSafeFileName(saved.getSpaceKey());
        String downloadUrl = storage.presignGet(saved.getSpaceKey(), safeFileName).url().toString();

        UploadEncounterAttachmentResponseVM body = UploadEncounterAttachmentResponseVM.ofEntity(saved, downloadUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    private static String getSafeFileName(String key) {
        int i = key.lastIndexOf('/');
        return i >= 0 ? key.substring(i + 1) : key;
    }

    /**
     * {@code : GET/ encounters/attachments/by-encounterId?encounterId=1,2: List active attachments for encounters.}
     * receive all files related to the encounter
     *
     * @param encounterId to get attachments by encounter
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}, a list of encounterAttachment view models in the body,
     **/
    @GetMapping("/encounters/attachments/by-encounterId")
    public ResponseEntity<List<EncounterAttachments>> list(@RequestParam List<Long> encounterId) {
        LOG.debug("Listing encounter attachments by encounterId list: {}", encounterId);
        return ResponseEntity.ok(service.list(encounterId));
    }

    /**
     * {@code : GET/ encounters/attachments/by-encounterIdAndSource/{encounterId}/{source}: List active attachments for an encounter by source and sourceId.}
     * recieve all files related to the encounter and source and sourceId
     *
     * @param encounterId to get attachments by encounter
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}, a list of encounterAttachment view models in the body,
     **/
    @GetMapping("/encounters/attachments/by-encounterIdAndSource/{encounterId}/{source}")
    public ResponseEntity<List<EncounterAttachments>> list(@PathVariable Long encounterId, @PathVariable EncounterAttachmentSource source, @RequestParam(required = false) Long sourceId) {
        LOG.debug("Listing encounter attachments by encounterId: {} and source: {} and sourceId: {}", encounterId, source, sourceId);
        return ResponseEntity.ok(service.listByEncounterIdAndSource(encounterId, source, sourceId));
    }

    /**
     * {@code : POST/ encounters/attachmentDownloadUrl/{id} } Presigned download URL.
     * return download url to download attachment
     *
     * @param id of attachment needs to download it
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}, an url and expires In seconds response
     */
    @PostMapping("/encounters/attachmentDownloadUrl/{id}")
    public ResponseEntity<DownloadEncounterAttachmentVM> downloadUrl(@PathVariable Long id) {
        LOG.debug("Downloading encounter attachment: {}", id);
        DownloadEncounterAttachmentVM ticket = service.downloadUrl(id);
        return ResponseEntity.ok(new DownloadEncounterAttachmentVM(ticket.url(), ticket.expiresInSeconds()));
    }

    /**
     * {@code DELETE/ encounters/attachments/{id} }Soft delete for an attachment without remove it from spaces
     *
     * @param id of attachment needs to delete
     */
    @DeleteMapping("/encounters/attachments/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("Deleting encounter attachment: {}", id);
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code PUT/ encounters/attachments/{id} } update type/details for encounter attachments
     * return updated encounter attachment
     *
     * @param id of attachment needs to update
     * @return the {@link ResponseEntity with status {@code 200 (OK)} when success and bad reuqest {@code 400 (Bad Request)} when error id not found}
     **/
    @PutMapping("/encounters/attachments/{id}")
    public ResponseEntity<EncounterAttachments> updateTypeAndDetails(@PathVariable Long id, @Valid @RequestBody UpdateEncounterAttachmentVM updateEncounterAttachmentVM) {
        LOG.debug("Updating encounter attachment: {}", updateEncounterAttachmentVM);

        EncounterAttachments encounterAttachments = repo.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Not found", ENTITY_NAME, "not_found"));

        if (updateEncounterAttachmentVM.type() != null) {
            encounterAttachments.setType(updateEncounterAttachmentVM.type());
        }
        if (updateEncounterAttachmentVM.details() != null) {
            encounterAttachments.setDetails(updateEncounterAttachmentVM.details());
        }

        EncounterAttachments saved = repo.save(encounterAttachments);
        return ResponseEntity.ok(saved);
    }


}
