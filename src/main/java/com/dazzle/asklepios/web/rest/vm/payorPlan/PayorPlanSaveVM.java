package com.dazzle.asklepios.web.rest.vm.payorPlan;


import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.PayorPlanType;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

public record PayorPlanSaveVM(

        @NotNull Long payorId,

        @NotBlank @Size(max = 255)
        String name,

        @NotNull PayorPlanType planType,

        @NotNull BillingItemTypes itemType,
        BigDecimal amount,
        @NotNull InsuranceCoverageType coverageType,

        Boolean isActive
) implements Serializable {}
