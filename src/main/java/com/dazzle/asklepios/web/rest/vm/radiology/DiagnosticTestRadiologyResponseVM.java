package com.dazzle.asklepios.web.rest.vm.radiology;

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
) {}
