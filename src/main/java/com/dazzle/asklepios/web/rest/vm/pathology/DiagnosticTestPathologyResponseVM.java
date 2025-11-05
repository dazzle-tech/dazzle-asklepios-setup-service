package com.dazzle.asklepios.web.rest.vm.pathology;

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
) {}
