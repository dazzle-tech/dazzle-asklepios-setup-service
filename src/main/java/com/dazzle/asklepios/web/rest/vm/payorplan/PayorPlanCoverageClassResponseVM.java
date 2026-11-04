package com.dazzle.asklepios.web.rest.vm.payorplan;

import com.dazzle.asklepios.domain.PayorPlanCoverageClass;
import com.dazzle.asklepios.domain.enumeration.CoverageClassType;

import java.time.Instant;

public record PayorPlanCoverageClassResponseVM(
        Long id,
        Long planId,
        CoverageClassType coverageClassType,
        String coverageClassValue,
        String coverageClassName,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {
    public static PayorPlanCoverageClassResponseVM ofEntity(PayorPlanCoverageClass c) {
        return new PayorPlanCoverageClassResponseVM(
                c.getId(),
                c.getPlan().getId(),
                c.getCoverageClassType(),
                c.getCoverageClassValue(),
                c.getCoverageClassName(),
                c.getIsActive(),
                c.getCreatedDate(),
                c.getLastModifiedDate()
        );
    }
}