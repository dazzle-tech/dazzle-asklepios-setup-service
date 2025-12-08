package com.dazzle.asklepios.web.rest.vm.payorplan;

import com.dazzle.asklepios.domain.PayorPlanItem;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;

import java.math.BigDecimal;
import java.time.Instant;

public record PayorPlanItemResponseVM(
        Long id,
        Long planId,
        BillingItemTypes itemType,
        BigDecimal amount,
        InsuranceCoverageType coverageType,
        Boolean isActive

) {
    public static PayorPlanItemResponseVM ofEntity(PayorPlanItem i) {
        return new PayorPlanItemResponseVM(
                i.getId(),
                i.getPlan().getId(),
                i.getItemType(),
                i.getAmount(),
                i.getCoverageType(),
                i.getIsActive()

        );
    }
}
