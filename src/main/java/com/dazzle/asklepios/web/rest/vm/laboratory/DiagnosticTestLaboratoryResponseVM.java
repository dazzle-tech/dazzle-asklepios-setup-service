package com.dazzle.asklepios.web.rest.vm.laboratory;


import com.dazzle.asklepios.domain.DiagnosticTestLaboratory;
import com.dazzle.asklepios.domain.enumeration.Method;
import com.dazzle.asklepios.domain.enumeration.Property;
import com.dazzle.asklepios.domain.enumeration.Scale;
import com.dazzle.asklepios.domain.enumeration.System;
import com.dazzle.asklepios.domain.enumeration.Timing;

import java.io.Serializable;
import java.util.Timer;

public record DiagnosticTestLaboratoryResponseVM(
        Long id,
        Long testId,
        Property property,
        System system,
        Scale scale,
        String reagents,
        Method method,
        Double testDurationTime,
        String timeUnit,
        String resultUnit,
        Boolean isProfile,
        String sampleContainer,
        Double sampleVolume,
        String sampleVolumeUnit,
        String tubeColor,
        String testDescription,
        String sampleHandling,
        Double turnaroundTime,
        String turnaroundTimeUnit,
        String preparationRequirements,
        String medicalIndications,
        String associatedRisks,
        String testInstructions,
        String category,
        String tubeType,
        Timing timing
) implements Serializable {

    public static DiagnosticTestLaboratoryResponseVM fromEntity(DiagnosticTestLaboratory e) {
        return new DiagnosticTestLaboratoryResponseVM(
                e.getId(),
                e.getTest() != null ? e.getTest().getId() : null,
                e.getProperty(),
                e.getSystem(),
                e.getScale(),
                e.getReagents(),
                e.getMethod(),
                e.getTestDurationTime(),
                e.getTimeUnit(),
                e.getResultUnit(),
                e.getIsProfile(),
                e.getSampleContainer(),
                e.getSampleVolume(),
                e.getSampleVolumeUnit(),
                e.getTubeColor(),
                e.getTestDescription(),
                e.getSampleHandling(),
                e.getTurnaroundTime(),
                e.getTurnaroundTimeUnit(),
                e.getPreparationRequirements(),
                e.getMedicalIndications(),
                e.getAssociatedRisks(),
                e.getTestInstructions(),
                e.getCategory(),
                e.getTubeType(),
                e.getTiming()
        );
    }
}
