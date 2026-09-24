package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.patient.YesNoQuestion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
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
        List<CoverageDiagnosisRefVM> diagnoses,
        List<Long> diagnosisIds,
        EncounterType encounterType,
        YesNoQuestion excludedResult,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {
    public CoverageExclusionVM {
        diagnoses = diagnoses == null ? List.of() : List.copyOf(diagnoses);
        diagnosisIds = diagnosisIds == null ? List.of() : List.copyOf(diagnosisIds);
    }
}
