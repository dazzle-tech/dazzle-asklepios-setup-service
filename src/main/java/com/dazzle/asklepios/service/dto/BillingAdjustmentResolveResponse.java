package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.DiscountApplicableOn;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import com.dazzle.asklepios.domain.enumeration.TaxApplicableOn;
import com.dazzle.asklepios.domain.enumeration.TaxCalculationType;
import com.dazzle.asklepios.domain.enumeration.TaxType;

import java.math.BigDecimal;

public record BillingAdjustmentResolveResponse(

        Long taxId,

        String taxCode,

        String taxName,

        TaxApplicableOn taxApplicableOn,

        TaxType taxType,

        TaxCalculationType taxCalculationType,

        BigDecimal taxRate,

        BigDecimal taxFixedAmount,

        Long discountId,

        String discountCode,

        String discountName,

        DiscountApplicableOn discountApplicableOn,

        DiscountType discountType,

        BigDecimal discountRate,

        BigDecimal discountFixedAmount

) {
}
