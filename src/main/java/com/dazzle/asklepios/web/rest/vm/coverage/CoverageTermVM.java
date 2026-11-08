package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageBasis;
import com.dazzle.asklepios.domain.enumeration.CoverageDiagnosisScope;
import com.dazzle.asklepios.domain.enumeration.CoveragePeriodBasis;
import com.dazzle.asklepios.domain.enumeration.CoverageTermType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoverageTermVM(
        Long id,
        CoverageTermType termType,
        CoverageDiagnosisScope diagnosisScope,
        Long diagnosisId,
        String diagnosisCode,
        String diagnosisName,
        List<CoverageDiagnosisRefVM> diagnoses,
        List<Long> diagnosisIds,
        Long facilityId,
        String facilityName,
        Boolean allDepartments,
        Long departmentId,
        String departmentName,
        EncounterType encounterType,
        CoveragePeriodBasis periodBasis,
        CoverageBasis coverageBasis,
        InsuranceCoverageType valueType,
        BigDecimal limitValue,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {
    public CoverageTermVM {
        diagnoses = diagnoses == null ? List.of() : List.copyOf(diagnoses);
        diagnosisIds = diagnosisIds == null ? List.of() : List.copyOf(diagnosisIds);
    }
}
