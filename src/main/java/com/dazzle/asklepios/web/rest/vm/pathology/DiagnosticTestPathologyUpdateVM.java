package com.dazzle.asklepios.web.rest.vm.pathology;

import jakarta.validation.constraints.NotNull;

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
) {}
