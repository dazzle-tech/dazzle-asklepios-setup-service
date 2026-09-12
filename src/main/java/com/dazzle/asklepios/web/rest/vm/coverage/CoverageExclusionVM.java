package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.patient.YesNoQuestion;

import java.time.Instant;

public record CoverageExclusionVM(
        Long id,
        CoverageRuleTarget exclusionType,
        BillingItemTypes billingItemType,
        Long serviceId,
        String serviceCode,
        String serviceName,
        Boolean allDiagnoses,
        Long diagnosisId,
        String diagnosisCode,
        String diagnosisName,
        EncounterType encounterType,
        YesNoQuestion excludedResult,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {}
