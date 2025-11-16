package com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransfer;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * View Model for uploading Encounter Attachments via REST.
 **/
public record UploadInventoryTransferAttachmentVM(
        @NotNull MultipartFile file
) implements Serializable {

    public static UploadInventoryTransferAttachmentVM ofEntity(MultipartFile file) {
        return new UploadInventoryTransferAttachmentVM(
                file
        );
    }
}
