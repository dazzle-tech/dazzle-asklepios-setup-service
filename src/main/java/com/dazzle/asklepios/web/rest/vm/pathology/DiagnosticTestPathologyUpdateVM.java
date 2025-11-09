package com.dazzle.asklepios.web.rest.vm.pathology;

import com.dazzle.asklepios.domain.DiagnosticTestPathology;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * ViewModel for updating DiagnosticTestPathology.
 */
public record DiagnosticTestPathologyUpdateVM(
        @NotNull Long id,
        @NotNull Long testId,
        @NotNull String category,
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

    public static DiagnosticTestPathology toEntity(DiagnosticTestPathologyUpdateVM vm) {
        DiagnosticTestPathology entity = DiagnosticTestPathologyCreateVM.toEntity(
                new DiagnosticTestPathologyCreateVM(
                        vm.testId(),
                        vm.category(),
                        vm.specimenType(),
                        vm.analysisProcedure(),
                        vm.turnaroundTime(),
                        vm.timeUnit(),
                        vm.testDescription(),
                        vm.sampleHandling(),
                        vm.medicalIndications(),
                        vm.criticalValues(),
                        vm.preparationRequirements(),
                        vm.associatedRisks()
                )
        );
        entity.setId(vm.id());
        return entity;
    }
}
