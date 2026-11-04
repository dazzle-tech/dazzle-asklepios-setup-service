package com.dazzle.asklepios.web.rest.vm.payorplan;

import com.dazzle.asklepios.domain.PayorPlan;
import com.dazzle.asklepios.domain.enumeration.CoverageType;
import com.dazzle.asklepios.domain.enumeration.biling.PayorPlanType;

import java.time.Instant;

public record PayorPlanResponseVM(
        Long id,
        Long payorId,
        String name,
        PayorPlanType planType,

        String networkId,
        CoverageType coverageType,
        String payerNphiesId,
        String waseelPlanId,

        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {
    public static PayorPlanResponseVM ofEntity(PayorPlan p) {
        return new PayorPlanResponseVM(
                p.getId(),
                p.getPayorId(),
                p.getName(),
                p.getPlanType(),

                p.getNetworkId(),
                p.getCoverageType(),
                p.getPayerNphiesId(),
                p.getWaseelPlanId(),

                p.getIsActive(),
                p.getCreatedDate(),
                p.getLastModifiedDate()
        );
    }
}