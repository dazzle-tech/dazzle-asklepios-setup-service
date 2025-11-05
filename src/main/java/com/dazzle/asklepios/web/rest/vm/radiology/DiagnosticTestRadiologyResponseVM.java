package com.dazzle.asklepios.web.rest.vm.radiology;

import com.dazzle.asklepios.domain.DiagnosticTestRadiology;
import java.io.Serializable;

/**
 * ViewModel for reading DiagnosticTestRadiology.
 */
public record DiagnosticTestRadiologyResponseVM(
        Long id,
        Long testId,
        String category,
        Double imageDuration,
        String testInstructions,
        String medicalIndications,
        String turnaroundTimeUnit,
        Double turnaroundTime,
        String associatedRisks
) implements Serializable {

    public static DiagnosticTestRadiologyResponseVM fromEntity(DiagnosticTestRadiology entity) {
        return new DiagnosticTestRadiologyResponseVM(
                entity.getId(),
                entity.getTest() != null ? entity.getTest().getId() : null,
                entity.getCategory(),
                entity.getImageDuration() != null ? Double.valueOf(entity.getImageDuration()) : null,
                entity.getTestInstructions(),
                entity.getMedicalIndications(),
                entity.getTurnaroundTimeUnit(),
                entity.getTurnaroundTime(),
                entity.getAssociatedRisks()
        );
    }
}
