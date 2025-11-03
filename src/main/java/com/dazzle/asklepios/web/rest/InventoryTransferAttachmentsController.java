package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.InventoryTransferAttachments;
import com.dazzle.asklepios.service.AttachmentStorageService;
import com.dazzle.asklepios.service.InventoryTransferAttachmentsService;
import com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransfer.DownloadInventoryTransferAttachmentVM;
import com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransfer.UploadInventoryTransferAttachmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransfer.UploadInventoryTransferAttachmentVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/setup")
@Validated
public class InventoryTransferAttachmentsController {
    private final InventoryTransferAttachmentsService service;
    private final AttachmentStorageService storage;

    private static final Logger LOG = LoggerFactory.getLogger(InventoryTransferAttachmentsController.class);

    public InventoryTransferAttachmentsController(InventoryTransferAttachmentsService service, AttachmentStorageService storage) {
        this.service = service;
        this.storage = storage;
    }
    /**
     * {@code : POST/ inventoryTransfer/{transactionId}/attachments}: Upload new attachment for an inventory transfer
     *  One call: receive file(s) → upload to Spaces → verify → insert row(s) → return pre-signed download URL(s).
     * @param uploadInventoryTransferAttachmentVM the upload payload.
     * @return the {@link UploadInventoryTransferAttachmentResponseVM} with status {@code 201 (Created)} and body of the uploaded attachment,
     *         or {@code 400 (Bad Request)} if the payload is invalid (mismatch size and type).
     **/
    @PostMapping(value = "/inventoryTransfer/{transactionId}/attachments", consumes = "multipart/form-data")
    public UploadInventoryTransferAttachmentResponseVM upload(@PathVariable Long transactionId, @ModelAttribute @Valid UploadInventoryTransferAttachmentVM uploadInventoryTransferAttachmentVM) {
       LOG.debug("Uploading inventory transfer attachment: {}", uploadInventoryTransferAttachmentVM);
       InventoryTransferAttachments saved = service.upload(transactionId, uploadInventoryTransferAttachmentVM);
        String safeFileName = getSafeFileName(saved.getSpaceKey());
        String downloadUrl = storage.presignGet(saved.getSpaceKey(), safeFileName).url().toString();

        return UploadInventoryTransferAttachmentResponseVM.ofEntity(saved, downloadUrl);
    }
    private static String getSafeFileName(String key) {
        int i = key.lastIndexOf('/');
        return i >= 0 ? key.substring(i + 1) : key;
    }
    /**
     * {@code : GET/ inventoryTransfer/attachments/by-transactionId?transactionId=1: List active attachments for inventoryTransfer.}
     * recieve all files related to the inventory transfer
     * @param transactionId to get attachments by inventory transfer
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}, a list of inventoryTransferAttachment view models in the body,
     **/
    @GetMapping("/inventoryTransfer/attachments/by-transactionId")
    public List<InventoryTransferAttachments> list(@RequestParam Long transactionId) {
        return service.list(transactionId);
    }
    

    /**
     * {@code : POST/ inventoryTransfer/attachmentDownloadUrl/{id} } Pre-signed download URL.
     * return download url to download attachment
     * @param id of attachment needs to download it
     * @return the {@link DownloadInventoryTransferAttachmentVM} with status {@code 200 (OK)}, an url and expires In seconds response
     * */
    @PostMapping("/inventoryTransfer/attachmentDownloadUrl/{id}")
    public DownloadInventoryTransferAttachmentVM downloadUrl(@PathVariable Long id) {
        LOG.debug("Downloading inventory transfer attachment: {}", id);
        DownloadInventoryTransferAttachmentVM downloadTicket = service.downloadUrl(id);
        return new DownloadInventoryTransferAttachmentVM(downloadTicket.url(), downloadTicket.expiresInSeconds());
    }

    /**
     * {@code DELETE/ inventoryTransfer/attachments/{id} }Soft delete for an attachment without remove it from spaces
     * @param id of attachment needs to delete
     * */
    @DeleteMapping("/inventoryTransfer/attachments/{id}")
    public void delete(@PathVariable Long id) {
        LOG.debug("Deleting inventory transfer attachment: {}", id);
        service.softDelete(id);
    }
    
}
