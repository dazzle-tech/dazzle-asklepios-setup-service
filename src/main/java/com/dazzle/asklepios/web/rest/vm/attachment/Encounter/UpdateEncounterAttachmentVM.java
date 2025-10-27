package com.dazzle.asklepios.web.rest.vm.attachment.Encounter;

import com.dazzle.asklepios.domain.EncounterAttachments;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * View Model for updating Encounter Attachment type and details via REST.
 **/
public record UpdateEncounterAttachmentVM(
        @Size(max = 100)
        String type,
        @Size(max = 1000)
        String details
) implements Serializable {

    public static UpdateEncounterAttachmentVM ofEntity(EncounterAttachments attachment) {
        return new UpdateEncounterAttachmentVM(
                attachment.getType(),
                attachment.getDetails()
        );
    }
}
