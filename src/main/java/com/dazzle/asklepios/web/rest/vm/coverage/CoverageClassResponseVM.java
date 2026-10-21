package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.CoverageClass;

import java.time.Instant;

public record CoverageClassResponseVM(
        Long id,
        Long coverageContractId,
        String name,
        Boolean isActive,
        Long insuranceCompanyPolicyId,
        String policyName,
        String policyCode,
        String policyNumber,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate

) {
    public static CoverageClassResponseVM of(CoverageClass coverageClass) {
        var policy = coverageClass.getInsuranceCompanyPolicy();
        return new CoverageClassResponseVM(
                coverageClass.getId(),
                coverageClass.getCoverageContract() == null ? null : coverageClass.getCoverageContract().getId(),
                coverageClass.getName(),
                coverageClass.getIsActive(),
                policy == null ? null : policy.getId(),
                policy == null ? null : policy.getPolicyName(),
                policy == null ? null : policy.getPolicyCode(),
                policy == null ? null : policy.getPolicyNumber(),
                coverageClass.getCreatedBy(),
                coverageClass.getCreatedDate(),
                coverageClass.getLastModifiedBy(),
                coverageClass.getLastModifiedDate()
        );
    }
}
