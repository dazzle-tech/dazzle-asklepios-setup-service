package com.dazzle.asklepios.web.rest.vm.laboratory;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestLaboratory;
import com.dazzle.asklepios.domain.enumeration.Method;
import com.dazzle.asklepios.domain.enumeration.Property;
import com.dazzle.asklepios.domain.enumeration.Scale;
import com.dazzle.asklepios.domain.enumeration.System;
import com.dazzle.asklepios.domain.enumeration.Timing;
import jakarta.validation.constraints.NotBlank;
import org.wildfly.common.annotation.NotNull;

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
        String tubeType,
        Timing timing
) implements Serializable {

    public DiagnosticTestLaboratory toEntity() {
        return DiagnosticTestLaboratory.builder()
                .test(DiagnosticTest.builder().id(testId).build())
                .property(property)
                .system(system)
                .scale(scale)
                .reagents(reagents)
                .method(method)
                .testDurationTime(testDurationTime)
                .timeUnit(timeUnit)
                .resultUnit(resultUnit)
                .isProfile(isProfile != null ? isProfile : false)
                .sampleContainer(sampleContainer)
                .sampleVolume(sampleVolume)
                .sampleVolumeUnit(sampleVolumeUnit)
                .tubeColor(tubeColor)
                .testDescription(testDescription)
                .sampleHandling(sampleHandling)
                .turnaroundTime(turnaroundTime)
                .turnaroundTimeUnit(turnaroundTimeUnit)
                .preparationRequirements(preparationRequirements)
                .medicalIndications(medicalIndications)
                .associatedRisks(associatedRisks)
                .testInstructions(testInstructions)
                .category(category)
                .tubeType(tubeType)
                .timing(timing)
                .build();
    }
}
