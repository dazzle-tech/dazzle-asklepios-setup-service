package com.dazzle.asklepios.web.rest.vm.radiology;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
) {}
