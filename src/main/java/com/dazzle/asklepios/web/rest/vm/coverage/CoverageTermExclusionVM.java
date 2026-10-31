package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoverageTermExclusionVM(
        Long id,
        Long termId,
        CoverageRuleTarget categoryScope,
        BillingItemTypes billingItemType,
        String subcategoryKey,
        String subcategoryLabel,
        Long serviceId,
        String serviceCode,
        String serviceName,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {}
