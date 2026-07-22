package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.DiscountApplicableOn;
import com.dazzle.asklepios.domain.enumeration.TaxApplicableOn;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;

public record BillingPricingResolveRequest(

        @NotNull
        Long facilityId,

        @NotNull
        Long patientId,

        @NotNull
        Long encounterId,

        @NotNull
        BillingItemTypes billingItemType,

        @NotNull
        Long sourceId,

        Long patientInsuranceId,

        Long payerId,

        @NotNull
        Currency currency,

        TaxApplicableOn taxApplicableOn,

        DiscountApplicableOn discountApplicableOn,

        LocalDate pricingDate

) implements Serializable {
}