package com.dazzle.asklepios.web.rest.vm.pathology;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestPathology;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * ViewModel for creating DiagnosticTestPathology.
 */
public record DiagnosticTestPathologyCreateVM(
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

    public static DiagnosticTestPathology toEntity(DiagnosticTestPathologyCreateVM vm) {
        return DiagnosticTestPathology.builder()
                .test(DiagnosticTest.builder().id(vm.testId()).build())
                .category(vm.category())
                .specimenType(vm.specimenType())
                .analysisProcedure(vm.analysisProcedure())
                .turnaroundTime(vm.turnaroundTime())
                .timeUnit(vm.timeUnit())
                .testDescription(vm.testDescription())
                .sampleHandling(vm.sampleHandling())
                .medicalIndications(vm.medicalIndications())
                .criticalValues(vm.criticalValues())
                .preparationRequirements(vm.preparationRequirements())
                .associatedRisks(vm.associatedRisks())
                .build();
    }
}
