package com.dazzle.asklepios.web.rest.vm.laboratory;

import com.dazzle.asklepios.domain.DiagnosticTestLaboratory;
import com.dazzle.asklepios.domain.enumeration.Method;
import com.dazzle.asklepios.domain.enumeration.Property;
import com.dazzle.asklepios.domain.enumeration.Scale;
import com.dazzle.asklepios.domain.enumeration.System;
import com.dazzle.asklepios.domain.enumeration.Timing;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record DiagnosticTestLaboratoryUpdateVM(
        @NotNull Long id,
        Long testId,
        Property property,
        System system,
        Scale scale,
        String reagents,
        Method method,
        Double testDurationTime,
        String timeUnit,
        @NotBlank  String resultUnit,
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
        @NotBlank String category,
        String tubeType,
        Timing timing
) implements Serializable {
    public DiagnosticTestLaboratory toEntity() {
        DiagnosticTestLaboratory entity = new DiagnosticTestLaboratoryCreateVM(
                testId, property, system, scale, reagents, method,
                testDurationTime, timeUnit, resultUnit, isProfile,
                sampleContainer, sampleVolume, sampleVolumeUnit, tubeColor,
                testDescription, sampleHandling, turnaroundTime, turnaroundTimeUnit,
                preparationRequirements, medicalIndications, associatedRisks,
                testInstructions, category, tubeType, timing
        ).toEntity();
        entity.setId(id);
        return entity;
    }
}
