package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageApprovalScope;
import com.dazzle.asklepios.domain.enumeration.CoveragePreApprovalLevel;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;

import java.math.BigDecimal;
import java.time.Instant;

public record CoveragePreApprovalVM(
        Long id,
        CoverageApprovalScope approvalScope,
        Long facilityId,
        String facilityName,
        Long departmentId,
        String departmentName,
        EncounterType encounterType,
        InsuranceCoverageType valueType,
        BigDecimal limitValue,
        CoveragePreApprovalLevel targetLevel,
        BillingItemTypes source,
        Long sourceId,
        String subcategoryKey,
        String scopeLabel,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {}
