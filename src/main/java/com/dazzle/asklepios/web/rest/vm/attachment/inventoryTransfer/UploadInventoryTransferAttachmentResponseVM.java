package com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransfer;

import com.dazzle.asklepios.domain.InventoryTransferAttachments;

import java.io.Serializable;

/**
 * View Model representing a response after uploading an Encounter Attachment.
 **/
public record UploadInventoryTransferAttachmentResponseVM(
        Long id,
        String filename,
        String mimeType,
        long sizeBytes,
        String downloadUrl

) implements Serializable {

    public static UploadInventoryTransferAttachmentResponseVM ofEntity(InventoryTransferAttachments attachment, String downloadUrl) {
        return new UploadInventoryTransferAttachmentResponseVM(
                attachment.getId(),
                attachment.getFilename(),
                attachment.getMimeType(),
                attachment.getSizeBytes(),
                downloadUrl
        );
    }
}