package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.biling.BillingCoverageType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BillingPricingResolutionRequest(

        @NotNull
        Long facilityId,

        @NotNull
        Long patientId,

        @NotNull
        Long encounterId,

        @NotNull
        BillingItemTypes billingItemType,

        @NotNull
        Long itemId,

        Long patientInsuranceId,

        Long payerId,

        BillingCoverageType coverageType,

        @NotNull
        Currency currency,

        @NotNull
        LocalDate pricingDate

) {
}