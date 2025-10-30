package com.dazzle.asklepios.web.rest.vm.attachment.patient;

import com.dazzle.asklepios.domain.PatientAttachments;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record UpdatePatientAttachmentVM (
    @Size(max = 100)
    String type,
    @Size(max = 1000)
    String details
) implements Serializable {
    public static UpdatePatientAttachmentVM ofEntity(PatientAttachments attachment) {
        return new UpdatePatientAttachmentVM(
                attachment.getType(),
                attachment.getDetails()
        );
    }
}
