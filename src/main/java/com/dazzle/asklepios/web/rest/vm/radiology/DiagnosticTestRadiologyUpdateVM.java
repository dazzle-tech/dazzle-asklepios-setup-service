package com.dazzle.asklepios.web.rest.vm.radiology;

import com.dazzle.asklepios.domain.DiagnosticTestRadiology;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * ViewModel for updating DiagnosticTestRadiology.
 */
public record DiagnosticTestRadiologyUpdateVM(
        @NotNull Long id,
        @NotNull Long testId,
        @NotBlank String category,
        Double imageDuration,
        String testInstructions,
        String medicalIndications,
        String turnaroundTimeUnit,
        Double turnaroundTime,
        String associatedRisks
) implements Serializable {

    public static DiagnosticTestRadiology toEntity(DiagnosticTestRadiologyUpdateVM vm) {
        DiagnosticTestRadiology entity = DiagnosticTestRadiologyCreateVM.toEntity(
                new DiagnosticTestRadiologyCreateVM(
                        vm.testId(),
                        vm.category(),
                        vm.imageDuration(),
                        vm.testInstructions(),
                        vm.medicalIndications(),
                        vm.turnaroundTimeUnit(),
                        vm.turnaroundTime(),
                        vm.associatedRisks()
                )
        );
        entity.setId(vm.id());
        return entity;
    }
}
