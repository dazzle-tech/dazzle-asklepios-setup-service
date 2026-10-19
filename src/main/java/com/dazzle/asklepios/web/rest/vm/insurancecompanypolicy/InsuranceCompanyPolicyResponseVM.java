package com.dazzle.asklepios.web.rest.vm.insurancecompanypolicy;

import com.dazzle.asklepios.domain.InsuranceCompanyPolicy;

import java.time.LocalDate;

public record InsuranceCompanyPolicyResponseVM(
        Long id,
        Long insuranceCompanyId,
        String policyName,
        String policyNumber,
        String policyCode,
        LocalDate validFrom,
        LocalDate validTo
) {
    public static InsuranceCompanyPolicyResponseVM ofEntity(InsuranceCompanyPolicy policy) {
        return new InsuranceCompanyPolicyResponseVM(
                policy.getId(),
                policy.getInsuranceCompany() != null ? policy.getInsuranceCompany().getId() : null,
                policy.getPolicyName(),
                policy.getPolicyNumber(),
                policy.getPolicyCode(),
                policy.getValidFrom(),
                policy.getValidTo()
        );
    }
}
