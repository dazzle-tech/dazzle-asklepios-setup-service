package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;

import java.time.Instant;

public record CoveragePreApprovalItemVM(
        Long id,
        CoverageRuleTarget itemType,
        ServiceCategory serviceCategory,
        Long serviceId,
        String serviceCode,
        String serviceName,
        Boolean allDiagnoses,
        Long diagnosisId,
        String diagnosisCode,
        String diagnosisName,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {}
