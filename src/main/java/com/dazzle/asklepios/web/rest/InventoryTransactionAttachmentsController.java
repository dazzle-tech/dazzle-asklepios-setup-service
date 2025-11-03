package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.InventoryTransactionAttachments;
import com.dazzle.asklepios.service.AttachmentStorageService;
import com.dazzle.asklepios.service.InventoryTransactionAttachmentsService;
import com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransaction.DownloadInventoryTransactionAttachmentVM;
import com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransaction.UploadInventoryTransactionAttachmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransaction.UploadInventoryTransactionAttachmentVM;
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
public class InventoryTransactionAttachmentsController {
    private final InventoryTransactionAttachmentsService service;
    private final AttachmentStorageService storage;

    private static final Logger LOG = LoggerFactory.getLogger(InventoryTransactionAttachmentsController.class);

    public InventoryTransactionAttachmentsController(InventoryTransactionAttachmentsService service, AttachmentStorageService storage) {
        this.service = service;
        this.storage = storage;
    }
    /**
     * {@code : POST/ inventoryTransaction/{transactionId}/attachments}: Upload new attachment for an inventory transaction
     *  One call: receive file(s) → upload to Spaces → verify → insert row(s) → return pre-signed download URL(s).
     * @param uploadInventoryTransactionAttachmentVM the upload payload.
     * @return the {@link UploadInventoryTransactionAttachmentResponseVM} with status {@code 201 (Created)} and body of the uploaded attachment,
     *         or {@code 400 (Bad Request)} if the payload is invalid (mismatch size and type).
     **/
    @PostMapping(value = "/inventoryTransaction/{transactionId}/attachments", consumes = "multipart/form-data")
    public UploadInventoryTransactionAttachmentResponseVM upload(@PathVariable Long transactionId, @ModelAttribute @Valid UploadInventoryTransactionAttachmentVM uploadInventoryTransactionAttachmentVM) {
       LOG.debug("Uploading inventory transaction attachment: {}", uploadInventoryTransactionAttachmentVM);
       InventoryTransactionAttachments saved = service.upload(transactionId, uploadInventoryTransactionAttachmentVM);
        String safeFileName = getSafeFileName(saved.getSpaceKey());
        String downloadUrl = storage.presignGet(saved.getSpaceKey(), safeFileName).url().toString();

        return UploadInventoryTransactionAttachmentResponseVM.ofEntity(saved, downloadUrl);
    }
    private static String getSafeFileName(String key) {
        int i = key.lastIndexOf('/');
        return i >= 0 ? key.substring(i + 1) : key;
    }
    /**
     * {@code : GET/ inventoryTransaction/attachments/by-transactionId?transactionId=1: List active attachments for inventoryTransaction.}
     * recieve all files related to the inventory transaction
     * @param transactionId to get attachments by inventory transaction
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}, a list of inventoryTransactionAttachment view models in the body,
     **/
    @GetMapping("/inventoryTransaction/attachments/by-transactionId")
    public List<InventoryTransactionAttachments> list(@RequestParam Long transactionId) {
        return service.list(transactionId);
    }
    

    /**
     * {@code : POST/ inventoryTransaction/attachmentDownloadUrl/{id} } Pre-signed download URL.
     * return download url to download attachment
     * @param id of attachment needs to download it
     * @return the {@link DownloadInventoryTransactionAttachmentVM} with status {@code 200 (OK)}, an url and expires In seconds response
     * */
    @PostMapping("/inventoryTransaction/attachmentDownloadUrl/{id}")
    public DownloadInventoryTransactionAttachmentVM downloadUrl(@PathVariable Long id) {
        LOG.debug("Downloading inventory transaction attachment: {}", id);
        DownloadInventoryTransactionAttachmentVM downloadTicket = service.downloadUrl(id);
        return new DownloadInventoryTransactionAttachmentVM(downloadTicket.url(), downloadTicket.expiresInSeconds());
    }

    /**
     * {@code DELETE/ inventoryTransaction/attachments/{id} }Soft delete for an attachment without remove it from spaces
     * @param id of attachment needs to delete
     * */
    @DeleteMapping("/inventoryTransaction/attachments/{id}")
    public void delete(@PathVariable Long id) {
        LOG.debug("Deleting inventory transaction attachment: {}", id);
        service.softDelete(id);
    }
    
}
