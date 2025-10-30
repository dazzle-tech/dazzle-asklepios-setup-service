package com.dazzle.asklepios.web.rest.vm.attachment.patient;

import com.dazzle.asklepios.domain.PatientAttachments;
import com.dazzle.asklepios.domain.enumeration.PatientAttachmentSource;

import java.io.Serializable;

public record UploadPatientAttachmentResponseVM(
        Long id,
        String filename,
        String mimeType,
        long sizeBytes,
        String type,
        String details,
        PatientAttachmentSource source,
        String downloadUrl

) implements Serializable {

    public static UploadPatientAttachmentResponseVM ofEntity(PatientAttachments attachment, String downloadUrl) {
        return new UploadPatientAttachmentResponseVM(
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
