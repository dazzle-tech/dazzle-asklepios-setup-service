package com.dazzle.asklepios.web.rest.vm.radiology;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record DiagnosticTestRadiologyCreateVM(
        @NotNull Long testId,
        @NotNull String category,
        Double imageDuration,
        String testInstructions,
        String medicalIndications,
        String turnaroundTimeUnit,
        Double turnaroundTime,
        String associatedRisks

) implements Serializable {}
