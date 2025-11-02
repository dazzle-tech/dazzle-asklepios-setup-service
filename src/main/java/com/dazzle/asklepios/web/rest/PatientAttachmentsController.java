package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.PatientAttachments;
import com.dazzle.asklepios.domain.enumeration.PatientAttachmentSource;
import com.dazzle.asklepios.repository.PatientAttachmentsRepository;
import com.dazzle.asklepios.service.AttachmentStorageService;
import com.dazzle.asklepios.service.PatientAttachmentsService;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.attachment.patient.DownloadPatientAttachmentVM;
import com.dazzle.asklepios.web.rest.vm.attachment.patient.UpdatePatientAttachmentVM;
import com.dazzle.asklepios.web.rest.vm.attachment.patient.UploadPatientAttachmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.attachment.patient.UploadPatientAttachmentVM;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/setup")
@Validated
public class PatientAttachmentsController {
    private final PatientAttachmentsService service;
    private final PatientAttachmentsRepository repo;
    private final AttachmentStorageService storage;

    private static final String ENTITY_NAME = "patientAttachments";
    private static final Logger LOG = LoggerFactory.getLogger(PatientAttachmentsController.class);


    public PatientAttachmentsController(PatientAttachmentsRepository repo, AttachmentStorageService storage, PatientAttachmentsService service) {
        this.repo = repo;
        this.storage = storage;
        this.service = service;
    }

    /**
     * {@code : POST/ patients/{patientId}/attachments}: Upload new attachment for a patient
     *  One call: receive file(s) → upload to Spaces → verify → insert row(s) → return presigned download URL(s).
     * @param uploadPatientAttachmentVM the upload payload.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and body of the uploaded attachment,
     *         or {@code 400 (Bad Request)} if the payload is invalid (mismatch size and type).
     **/
    @PostMapping(value = "/patients/{patientId}/attachments", consumes = "multipart/form-data")
    public UploadPatientAttachmentResponseVM upload(@PathVariable Long patientId, @ModelAttribute @Valid UploadPatientAttachmentVM uploadPatientAttachmentVM) {
        LOG.debug("Uploading patient attachment: {}", uploadPatientAttachmentVM);
        PatientAttachments saved = service.upload(patientId, uploadPatientAttachmentVM);
        String safeFileName = getSafeFileName(saved.getSpaceKey());
        String downloadUrl = storage.presignGet(saved.getSpaceKey(), safeFileName).url().toString();

        return UploadPatientAttachmentResponseVM.ofEntity(saved, downloadUrl);
    }
    private static String getSafeFileName(String key) {
        int i = key.lastIndexOf('/');
        return i >= 0 ? key.substring(i + 1) : key;
    }
    /**
     * {@code : GET/ patients/attachments/by-patientId/{patientId}: List active attachments for a patient.}
     * receive all files related to the patient
     * @param patientId to get attachments by patient
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}, a list of patientAttachments view models in the body,
     **/
    @GetMapping("/patients/attachments/by-patientId/{patientId}")
    public List<PatientAttachments> list(@PathVariable Long patientId) {
        LOG.debug("Listing patient attachments by patientId: {}", patientId);
        return service.list(patientId);
    }

    /**
     * {@code : POST/ patients/attachmentDownloadUrl/{id} } Presigned download URL.
     * return download url to download attachment
     * @param id of attachment needs to download it
     * @return the {@link DownloadPatientAttachmentVM} with status {@code 200 (OK)}, an url and expires In seconds response
     * */
    @PostMapping("/patients/attachmentDownloadUrl/{id}")
    public DownloadPatientAttachmentVM downloadUrl(@PathVariable Long id) {
        LOG.debug("Downloading patient attachment: {}", id);
        DownloadPatientAttachmentVM downloadTicket = service.downloadUrl(id);
        return new DownloadPatientAttachmentVM(downloadTicket.url(), downloadTicket.expiresInSeconds());
    }

    /**
     * {@code DELETE/ patients/attachments/{id} }Soft delete for an attachment without remove it from spaces
     * @param id of attachment needs to delete
     * */
    @DeleteMapping("/patients/attachments/{id}")
    public void delete(@PathVariable Long id) {
        LOG.debug("Deleting patient attachment: {}", id);
        service.softDelete(id);
    }

    /**
     * {@code PUT/ patients/attachments/{id} } update type/details for patient attachments
     * return updated patient attachment
     * @param id of attachment needs to update
     * @return the {@link PatientAttachments with status {@code 200 (OK)} when success and bad reuqest {@code 400 (Bad Request)} when error id not found}
     * **/
    @PutMapping("/patients/attachments/{id}")
    public PatientAttachments updateTypeAndDetails(@PathVariable Long id, @Valid @RequestBody UpdatePatientAttachmentVM updatePatientAttachmentVM) {
        LOG.debug("Updating patient attachment: {}", updatePatientAttachmentVM);

        PatientAttachments patientAttachments = repo.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Not found", ENTITY_NAME, "not_found"));

        if (updatePatientAttachmentVM.type() != null) patientAttachments.setType(updatePatientAttachmentVM.type());
        if (updatePatientAttachmentVM.details() != null) patientAttachments.setDetails(updatePatientAttachmentVM.details());

        return repo.save(patientAttachments);
    }

    /**
     * {@code : GET/ patients/{patientId}/profile-picture: last uploaded profile picture for a patient -> Presigned download URL.}
     * receive last profile picture related to the patient
     * @param patientId to get attachments by patient
     * @return the {@link DownloadPatientAttachmentVM} with status {@code 200 (OK)}, an url and expires In seconds response
     **/
    @GetMapping("/patients/{patientId}/profile-picture")
    @ResponseStatus(HttpStatus.OK)
    public DownloadPatientAttachmentVM getLatestProfilePicture(@PathVariable Long patientId) {
        LOG.debug("last profile picture for patient: {}", patientId);
        PatientAttachments patientAttachments = repo.findFirstByPatientIdAndSourceAndDeletedAtIsNullOrderByCreatedDateDesc(patientId, PatientAttachmentSource.PATIENT_PROFILE_PICTURE)
                .orElseThrow(() -> new BadRequestAlertException("No profile picture", ENTITY_NAME, "not_found"));
        DownloadPatientAttachmentVM downloadTicket = service.downloadUrl(patientAttachments.getId());
        return new DownloadPatientAttachmentVM(downloadTicket.url(), downloadTicket.expiresInSeconds());
    }
}
