package com.dazzle.asklepios.web.rest.vm.payorPlan;

import com.dazzle.asklepios.domain.PayorPlan;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.PayorPlanType;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;

import java.math.BigDecimal;
import java.time.Instant;

public record PayorPlanResponseVM(
        Long id,
        Long payorId,

        String name,
        PayorPlanType planType,

        BillingItemTypes itemType,
        BigDecimal amount,
        InsuranceCoverageType coverageType,

        Boolean isActive,

        Instant createdDate,
        Instant lastModifiedDate
) {
    public static PayorPlanResponseVM ofEntity(PayorPlan p) {
        return new PayorPlanResponseVM(
                p.getId(),
                p.getPayor().getId(),
                p.getName(),
                p.getPlanType(),
                p.getItemType(),
                p.getAmount(),
                p.getCoverageType(),
                p.getIsActive(),
                p.getCreatedDate(),
                p.getLastModifiedDate()
        );
    }
}
