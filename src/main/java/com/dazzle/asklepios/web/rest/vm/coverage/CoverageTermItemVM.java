package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;

import java.math.BigDecimal;
import java.time.Instant;

public record CoverageTermItemVM(
        Long id,
        CoverageRuleTarget categoryScope,
        BillingItemTypes billingItemType,
        Long serviceId,
        String serviceCode,
        String serviceName,
        InsuranceCoverageType valueType,
        BigDecimal limitValue,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {}
