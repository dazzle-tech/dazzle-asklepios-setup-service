package com.dazzle.asklepios.web.rest.vm.payorplan;

import com.dazzle.asklepios.domain.enumeration.CoverageClassType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record PayorPlanCoverageClassSaveVM(

        @NotNull
        Long planId,

        @NotNull
        CoverageClassType coverageClassType,

        @NotBlank
        @Size(max = 100)
        String coverageClassValue,

        @Size(max = 255)
        String coverageClassName,

        Boolean isActive

) implements Serializable {}