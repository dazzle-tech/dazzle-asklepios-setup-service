package com.dazzle.asklepios.web.rest.vm.payorplan;

import java.io.Serializable;
import java.util.List;

public record CchiPayorPlanUpsertVM(
        Long payorId,
        String planId,
        String payerNphiesId,
        String coverageType,
        String networkId,
        String policyClassName,
        List<CchiCoverageClassVM> coverageClassList
) implements Serializable {
}