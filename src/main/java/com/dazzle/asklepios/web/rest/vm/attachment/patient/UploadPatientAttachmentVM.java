package com.dazzle.asklepios.web.rest.vm.attachment.patient;

import com.dazzle.asklepios.domain.PatientAttachments;
import com.dazzle.asklepios.domain.enumeration.PatientAttachmentSource;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

public record UploadPatientAttachmentVM(
        String type,
        String details,
        @NotNull PatientAttachmentSource source,
        @NotNull MultipartFile file
) implements Serializable {

    public static UploadPatientAttachmentVM ofEntity(PatientAttachments attachment, MultipartFile file) {
        return new UploadPatientAttachmentVM(
                attachment.getType(),
                attachment.getDetails(),
                attachment.getSource(),
                file
        );
    }
}
