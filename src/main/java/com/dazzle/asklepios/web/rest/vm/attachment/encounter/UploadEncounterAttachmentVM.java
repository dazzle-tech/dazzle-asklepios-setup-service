package com.dazzle.asklepios.web.rest.vm.attachment.encounter;

import com.dazzle.asklepios.domain.EncounterAttachments;
import com.dazzle.asklepios.domain.enumeration.EncounterAttachmentSource;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * View Model for uploading Encounter Attachments via REST.
 **/
public record UploadEncounterAttachmentVM(
        String type,
        String details,
        @NotNull EncounterAttachmentSource source,
        @NotNull long sourceId,
        @NotNull MultipartFile file
) implements Serializable {

    public static UploadEncounterAttachmentVM ofEntity(EncounterAttachments attachment, MultipartFile file) {
        return new UploadEncounterAttachmentVM(
                attachment.getType(),
                attachment.getDetails(),
                attachment.getSource(),
                attachment.getSourceId(),
                file
        );
    }
}
