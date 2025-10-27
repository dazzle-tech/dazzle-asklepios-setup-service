package com.dazzle.asklepios.web.rest.vm.attachment.Encounter;
import com.dazzle.asklepios.domain.EncounterAttachments;
import com.dazzle.asklepios.domain.enumeration.EncounterAttachmentSource;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

/**
 * View Model for uploading Encounter Attachments via REST.
 **/
public record UploadEncounterAttachmentVM(
        String type,
        String details,
        EncounterAttachmentSource source,
        @NotNull List<MultipartFile> files
) implements Serializable {

    public static UploadEncounterAttachmentVM ofEntity(EncounterAttachments attachment, List<MultipartFile> files) {
        return new UploadEncounterAttachmentVM(
                attachment.getType(),
                attachment.getDetails(),
                attachment.getSource(),
                files
        );
    }
}
