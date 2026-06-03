package com.dazzle.asklepios.web.rest.vm.payorplan;

import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record PayorPlanItemSaveVM(

        @NotNull Long planId,

        @NotNull BillingItemTypes itemType,

        BigDecimal amount,

        @NotNull InsuranceCoverageType coverageType,

        Boolean isActive,

        Boolean preAuthorization,

        Long brandMedicationId,

        Long diagnosticTestId,

        Long serviceId,

        Long procedureId

) implements Serializable {}
