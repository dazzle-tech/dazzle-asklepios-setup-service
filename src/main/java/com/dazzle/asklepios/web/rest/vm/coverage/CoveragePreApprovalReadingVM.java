package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageApprovalScope;
import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;

import java.util.List;

public record CoveragePreApprovalReadingVM(
        Long preApprovalId,
        Long itemId,
        CoverageApprovalScope approvalScope,
        Long facilityId,
        Long departmentId,
        EncounterType encounterType,
        CoverageRuleTarget itemType,
        ServiceCategory serviceCategory,
        Long serviceId,
        Boolean allDiagnoses,
        Long diagnosisId,
        List<Long> diagnosisIds
) {
    public CoveragePreApprovalReadingVM {
        diagnosisIds = diagnosisIds == null ? List.of() : List.copyOf(diagnosisIds);
    }
}
