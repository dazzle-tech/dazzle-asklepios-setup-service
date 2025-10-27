package com.dazzle.asklepios.web.rest.vm.attachment.Encounter;
import com.dazzle.asklepios.domain.EncounterAttachments;
import com.dazzle.asklepios.domain.enumeration.EncounterAttachmentSource;

import java.io.Serializable;

/**
 * View Model representing a response after uploading an Encounter Attachment.
 **/
public record UploadEncounterAttachmentResponseVM(
        Long id,
        String filename,
        String mimeType,
        long sizeBytes,
        String type,
        String details,
        EncounterAttachmentSource source,
        String downloadUrl
) implements Serializable {

    public static UploadEncounterAttachmentResponseVM ofEntity(EncounterAttachments attachment, String downloadUrl) {
        return new UploadEncounterAttachmentResponseVM(
                attachment.getId(),
                attachment.getFilename(),
                attachment.getMimeType(),
                attachment.getSizeBytes(),
                attachment.getType(),
                attachment.getDetails(),
                attachment.getSource(),
                downloadUrl
        );
    }
}