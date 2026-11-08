package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.Currency;

import java.math.BigDecimal;

public record BillingPricingResolutionDTO(

        Long priceListSetupId,

        Long priceListSetupItemId,

        String priceListName,

        String itemCode,

        String itemName,

        BigDecimal unitPrice,

        Currency currency,

        Long discountId,

        String discountType,

        BigDecimal discountRate,

        BigDecimal discountAmount,

        Long taxId,

        String taxType,

        BigDecimal taxRate,

        String calculationOrder,

        String roundingMode,

        Integer roundingScale,

        Boolean requiresPreAuthorization,

        String priceListType,

        Boolean cashFallback

) {
}