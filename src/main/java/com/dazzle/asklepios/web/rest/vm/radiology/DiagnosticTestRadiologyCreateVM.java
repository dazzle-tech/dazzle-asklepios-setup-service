package com.dazzle.asklepios.web.rest.vm.radiology;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestRadiology;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * ViewModel for creating DiagnosticTestRadiology.
 */
public record DiagnosticTestRadiologyCreateVM(
        @NotNull Long testId,
        @NotNull String category,
        Double imageDuration,
        String testInstructions,
        String medicalIndications,
        String turnaroundTimeUnit,
        Double turnaroundTime,
        String associatedRisks
) implements Serializable {

    public static DiagnosticTestRadiology toEntity(DiagnosticTestRadiologyCreateVM vm) {
        return DiagnosticTestRadiology.builder()
                .test(DiagnosticTest.builder().id(vm.testId()).build())
                .category(vm.category())
                .imageDuration(vm.imageDuration() != null ? vm.imageDuration(): null)
                .testInstructions(vm.testInstructions())
                .medicalIndications(vm.medicalIndications())
                .turnaroundTimeUnit(vm.turnaroundTimeUnit())
                .turnaroundTime(vm.turnaroundTime())
                .associatedRisks(vm.associatedRisks())
                .build();
    }
}
