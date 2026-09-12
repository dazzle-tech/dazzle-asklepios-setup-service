package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageApprovalScope;
import com.dazzle.asklepios.domain.enumeration.EncounterType;

import java.time.Instant;

public record CoveragePreApprovalVM(
        Long id,
        CoverageApprovalScope approvalScope,
        Long facilityId,
        String facilityName,
        Long departmentId,
        String departmentName,
        EncounterType encounterType,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {}
