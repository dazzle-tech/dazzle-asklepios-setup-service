package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageApprovalScope;
import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;

import java.math.BigDecimal;
import java.util.List;

public record CoveragePreApprovalReadingVM(
        Long preApprovalId,
        Long itemId,
        CoverageApprovalScope approvalScope,
        Long facilityId,
        Long departmentId,
        EncounterType encounterType,
        CoverageRuleTarget itemType,
        BillingItemTypes serviceCategory,
        Long serviceId,
        Boolean allDiagnoses,
        Long diagnosisId,
        List<Long> diagnosisIds,
        InsuranceCoverageType valueType,
        BigDecimal limitValue
) {
    public CoveragePreApprovalReadingVM {
        diagnosisIds = diagnosisIds == null ? List.of() : List.copyOf(diagnosisIds);
    }
}
