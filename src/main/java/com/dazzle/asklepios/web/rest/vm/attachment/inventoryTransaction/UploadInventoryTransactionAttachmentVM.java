package com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransaction;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * View Model for uploading Encounter Attachments via REST.
 **/
public record UploadInventoryTransactionAttachmentVM(
        @NotNull MultipartFile file
) implements Serializable {

    public static UploadInventoryTransactionAttachmentVM ofEntity(MultipartFile file) {
        return new UploadInventoryTransactionAttachmentVM(
                file
        );
    }
}
