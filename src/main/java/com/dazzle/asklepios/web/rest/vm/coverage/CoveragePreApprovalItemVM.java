package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
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
        List<CoverageDiagnosisRefVM> diagnoses,
        List<Long> diagnosisIds,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {
    public CoveragePreApprovalItemVM {
        diagnoses = diagnoses == null ? List.of() : List.copyOf(diagnoses);
        diagnosisIds = diagnosisIds == null ? List.of() : List.copyOf(diagnosisIds);
    }
}
