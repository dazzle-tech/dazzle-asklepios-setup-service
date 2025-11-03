package com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransaction;

import com.dazzle.asklepios.domain.InventoryTransactionAttachments;

import java.io.Serializable;

/**
 * View Model representing a response after uploading an Encounter Attachment.
 **/
public record UploadInventoryTransactionAttachmentResponseVM(
        Long id,
        String filename,
        String mimeType,
        long sizeBytes,
        String downloadUrl

) implements Serializable {

    public static UploadInventoryTransactionAttachmentResponseVM ofEntity(InventoryTransactionAttachments attachment, String downloadUrl) {
        return new UploadInventoryTransactionAttachmentResponseVM(
                attachment.getId(),
                attachment.getFilename(),
                attachment.getMimeType(),
                attachment.getSizeBytes(),
                downloadUrl
        );
    }
}