package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;

import java.math.BigDecimal;
import java.time.Instant;

public record CoverageDiscountVM(
        Long id,
        CoverageRuleTarget targetType,
        BillingItemTypes billingItemType,
        Long serviceId,
        String serviceCode,
        String serviceName,
        EncounterType encounterType,
        DiscountType discountType,
        BigDecimal discountValue,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {}
