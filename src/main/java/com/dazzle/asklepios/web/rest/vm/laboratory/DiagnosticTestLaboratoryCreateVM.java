package com.dazzle.asklepios.web.rest.vm.laboratory;


import com.dazzle.asklepios.domain.enumeration.Method;
import com.dazzle.asklepios.domain.enumeration.Property;
import com.dazzle.asklepios.domain.enumeration.Scale;
import com.dazzle.asklepios.domain.enumeration.System;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record DiagnosticTestLaboratoryCreateVM(
        @NotNull Long testId,
        Property property,
        System system,
        Scale scale,
        String reagents,
        Method method,
        Double testDurationTime,
        String timeUnit,
        @NotBlank String resultUnit,
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
        String tubeType
) implements Serializable {}
