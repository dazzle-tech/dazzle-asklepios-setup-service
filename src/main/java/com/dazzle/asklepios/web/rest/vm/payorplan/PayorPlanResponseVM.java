package com.dazzle.asklepios.web.rest.vm.payorplan;

import com.dazzle.asklepios.domain.PayorPlan;
import com.dazzle.asklepios.domain.enumeration.biling.PayorPlanType;
import com.dazzle.asklepios.web.rest.vm.payorplan.PayorPlanItemResponseVM;
import java.time.Instant;
import java.util.List;

public record PayorPlanResponseVM(
        Long id,
        Long payorId,
        String name,
        PayorPlanType planType,
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
                p.getIsActive(),
                p.getCreatedDate(),
                p.getLastModifiedDate()

        );
    }
}
