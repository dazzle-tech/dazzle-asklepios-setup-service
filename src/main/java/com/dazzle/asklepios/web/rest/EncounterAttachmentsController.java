package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.EncounterAttachments;
import com.dazzle.asklepios.domain.enumeration.EncounterAttachmentSource;
import com.dazzle.asklepios.repository.EncounterAttachementsRepository;
import com.dazzle.asklepios.service.AttachmentStorageService;
import com.dazzle.asklepios.service.EncounterAttachmentsService;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.attachment.encounter.UploadEncounterAttachmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.attachment.encounter.UpdateEncounterAttachmentVM;
import com.dazzle.asklepios.web.rest.vm.attachment.encounter.UploadEncounterAttachmentVM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;


@RestController
@RequestMapping("/api/setup")
@Validated
public class EncounterAttachmentsController {
    private final EncounterAttachmentsService service;
    private final EncounterAttachementsRepository repo;
    private final AttachmentStorageService storage;

    private static final String ENTITY_NAME = "EncounterAttachments";

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentController.class);

    public EncounterAttachmentsController(EncounterAttachmentsService service, EncounterAttachementsRepository repo, AttachmentStorageService storage) {
        this.service = service;
        this.repo = repo;
        this.storage = storage;
    }
    /**
     * {@Code: POST/ encounters/{encounterId}/attachments}: Upload new attachment for an encounter
     *  One call: receive file(s) → upload to Spaces → verify → insert row(s) → return presigned download URL(s).
     * @param uploadEncounterAttachmentVM the upload payload.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and body of the uploaded attachment,
     *         or {@code 400 (Bad Request)} if the payload is invalid (mismatch size and type).
     **/
    @PostMapping(value = "/encounters/{encounterId}/attachments", consumes = "multipart/form-data")
    public UploadEncounterAttachmentResponseVM upload(@PathVariable Long encounterId, @ModelAttribute @Valid UploadEncounterAttachmentVM uploadEncounterAttachmentVM) {
       LOG.debug("Uploading encounter attachment: {}", uploadEncounterAttachmentVM);
       EncounterAttachments saved = service.upload(encounterId, uploadEncounterAttachmentVM);
        String safeFileName = getSafeFileName(saved.getSpaceKey());
        String downloadUrl = storage.presignGet(saved.getSpaceKey(), safeFileName).url().toString();

        return UploadEncounterAttachmentResponseVM.ofEntity(saved, downloadUrl);
    }
    private static String getSafeFileName(String key) {
        int i = key.lastIndexOf('/');
        return i >= 0 ? key.substring(i + 1) : key;
    }
    /**
     * {@Code: GET/ encounters/attachments/by-encounterId?enounterId=1,2: List active attachments for encounters.}
     * recieve all files related to the encounter
     * @param encounterId to get attachments by encounter
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}, a list of encounterAttachment view models in the body,
     **/
    @GetMapping("/encounters/attachments/by-encounterId")
    public List<EncounterAttachments> list(@RequestParam List<Long> encounterId) {
        return service.list(encounterId);
    }

    /**
     * {@Code: GET/ encounters/attachments-list-by-encounterIdAndSource/{encounterId}/{source}: List active attachments for an encounter by source.}
     * recieve all files related to the encounter and source
     * @param encounterId to get attachments by encounter
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}, a list of encounterAttachment view models in the body,
     **/
    @GetMapping("/encounters/attachments/by-encounterIdAndSource/{encounterId}/{source}")
    public List<EncounterAttachments> list(@PathVariable Long encounterId, @PathVariable EncounterAttachmentSource source) {
        LOG.debug("Listing encounter attachments by encounterId: {} and source : {}", encounterId,source);
        return service.listByEncounterIdAndSource(encounterId, source);
    }

    /**
     * {@code : POST/ encounters/attachmentDownloadUrl/{id} } Presigned download URL.
     * return download url to download attachment
     * @param id of attachment needs to download it
     * @return the {@link com.dazzle.asklepios.service.EncounterAttachmentsService.DownloadTicket} with status {@code 200 (OK)}, an url and expires In seconds response
     * */
    @PostMapping("/encounters/attachmentDownloadUrl/{id}")
    public EncounterAttachmentsService.DownloadTicket downloadUrl(@PathVariable Long id) {
        LOG.debug("Downloading encounter attachment: {}", id);
        EncounterAttachmentsService.DownloadTicket downloadTicket = service.downloadUrl(id);
        return new EncounterAttachmentsService.DownloadTicket(downloadTicket.url(), downloadTicket.expiresInSeconds());
    }

    /**
     * {@code DELETE/ encounters/attachments/{id} }Soft delete for an attachment without remove it from spaces
     * @param id of attachment needs to delete
     * */
    @DeleteMapping("/encounters/attachments/{id}")
    public void delete(@PathVariable Long id) {
        LOG.debug("Deleting encounter attachment: {}", id);
        service.softDelete(id);
    }

    /**
     * {@code PUT/ encounters/attachments/{id} } update type/details for encounter attachments
     * return updated encounter attachment
     * @param id of attachment needs to update
     * @return the {@link EncounterAttachments with status {@code 200 (OK)} when success and bad reuqest {@code 400 (Bad Request)} when error id not found}
     * **/
    @PutMapping("/encounters/attachments/{id}")
    public EncounterAttachments updateTypeAndDetails(@PathVariable Long id, @Valid @RequestBody UpdateEncounterAttachmentVM updateEncounterAttachmentVM) {
        LOG.debug("Updating encounter attachment: {}", updateEncounterAttachmentVM);
        EncounterAttachments encounterAttachments = repo.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Not found", ENTITY_NAME, "not_found"));
        if (updateEncounterAttachmentVM.type() != null) {
            encounterAttachments.setType(updateEncounterAttachmentVM.type());
        }
        if (updateEncounterAttachmentVM.details() != null) {
            encounterAttachments.setDetails(updateEncounterAttachmentVM.details());
        }
        return repo.save(encounterAttachments);
    }


}
