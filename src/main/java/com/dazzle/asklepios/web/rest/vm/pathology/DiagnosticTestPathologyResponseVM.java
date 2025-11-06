package com.dazzle.asklepios.web.rest.vm.pathology;

import com.dazzle.asklepios.domain.DiagnosticTestPathology;
import java.io.Serializable;

/**
 * ViewModel for reading DiagnosticTestPathology.
 */
public record DiagnosticTestPathologyResponseVM(
        Long id,
        Long testId,
        String category,
        String specimenType,
        String analysisProcedure,
        Double turnaroundTime,
        String timeUnit,
        String testDescription,
        String sampleHandling,
        String medicalIndications,
        String criticalValues,
        String preparationRequirements,
        String associatedRisks
) implements Serializable {

    public static DiagnosticTestPathologyResponseVM fromEntity(DiagnosticTestPathology e) {
        return new DiagnosticTestPathologyResponseVM(
                e.getId(),
                e.getTest() != null ? e.getTest().getId() : null,
                e.getCategory(),
                e.getSpecimenType(),
                e.getAnalysisProcedure(),
                e.getTurnaroundTime(),
                e.getTimeUnit(),
                e.getTestDescription(),
                e.getSampleHandling(),
                e.getMedicalIndications(),
                e.getCriticalValues(),
                e.getPreparationRequirements(),
                e.getAssociatedRisks()
        );
    }
}
