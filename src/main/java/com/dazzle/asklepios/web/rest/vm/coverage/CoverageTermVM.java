package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageBasis;
import com.dazzle.asklepios.domain.enumeration.CoverageDiagnosisScope;
import com.dazzle.asklepios.domain.enumeration.CoveragePeriodBasis;
import com.dazzle.asklepios.domain.enumeration.CoverageTermType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;

import java.math.BigDecimal;
import java.time.Instant;

public record CoverageTermVM(
        Long id,
        CoverageTermType termType,
        CoverageDiagnosisScope diagnosisScope,
        Long diagnosisId,
        String diagnosisCode,
        String diagnosisName,
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
) {}
