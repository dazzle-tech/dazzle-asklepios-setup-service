package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.DiscountApplicableOn;
import com.dazzle.asklepios.domain.enumeration.TaxApplicableOn;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BillingAdjustmentResolveRequest(

        @NotNull
        Long facilityId,

        Currency currency,

        TaxApplicableOn taxApplicableOn,

        DiscountApplicableOn discountApplicableOn,

        LocalDate pricingDate

) {
}
