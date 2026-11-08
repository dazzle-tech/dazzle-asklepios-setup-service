package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageClass;
import com.dazzle.asklepios.domain.InsuranceCompanyPolicy;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.repository.CoverageClassRepository;
import com.dazzle.asklepios.repository.InsuranceCompanyPolicyRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.web.rest.vm.insuranceregistration.InsuranceRegistrationStatusVM;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Decides whether a patient insurance or visit may be registered on an insurance company,
 * its policy and its class. The first inactive level wins: company, then policy, then class.
 * Unknown companies, policies or classes are not blocked here.
 */
@Service
@Transactional(readOnly = true)
public class InsuranceRegistrationStatusService {

    private final NphiesPayerRepository nphiesPayerRepository;
    private final InsuranceCompanyPolicyRepository insuranceCompanyPolicyRepository;
    private final CoverageClassRepository coverageClassRepository;

    public InsuranceRegistrationStatusService(
            NphiesPayerRepository nphiesPayerRepository,
            InsuranceCompanyPolicyRepository insuranceCompanyPolicyRepository,
            CoverageClassRepository coverageClassRepository
    ) {
        this.nphiesPayerRepository = nphiesPayerRepository;
        this.insuranceCompanyPolicyRepository = insuranceCompanyPolicyRepository;
        this.coverageClassRepository = coverageClassRepository;
    }

    public InsuranceRegistrationStatusVM check(String payerNphiesId, String policyNumber, String className) {
        NphiesPayer payer = findPayer(payerNphiesId).orElse(null);
        if (payer == null) {
            return InsuranceRegistrationStatusVM.allowedStatus();
        }
        if (isInactive(payer.getIsActive())) {
            return InsuranceRegistrationStatusVM.blocked(
                    "Insurance company " + payerName(payer),
                    payer.getInactiveReason()
            );
        }

        InsuranceCompanyPolicy policy = findPolicy(payer.getId(), policyNumber).orElse(null);
        if (policy == null) {
            return InsuranceRegistrationStatusVM.allowedStatus();
        }
        if (isInactive(policy.getIsActive())) {
            return InsuranceRegistrationStatusVM.blocked(
                    "Policy " + policyLabel(policy),
                    policy.getInactiveReason()
            );
        }

        return findOnlyInactiveClass(policy.getId(), className)
                .map(coverageClass -> InsuranceRegistrationStatusVM.blocked(
                        "Class " + coverageClass.getName() + " of policy " + policyLabel(policy),
                        coverageClass.getInactiveReason()
                ))
                .orElseGet(InsuranceRegistrationStatusVM::allowedStatus);
    }

    private Optional<NphiesPayer> findPayer(String payerNphiesId) {
        return hasText(payerNphiesId)
                ? nphiesPayerRepository.findFirstByNphiesIdIgnoreCase(payerNphiesId.trim())
                : Optional.empty();
    }

    private Optional<InsuranceCompanyPolicy> findPolicy(Long payerId, String policyNumber) {
        return hasText(policyNumber)
                ? insuranceCompanyPolicyRepository.findFirstByInsuranceCompany_IdAndPolicyNumberIgnoreCase(payerId, policyNumber.trim())
                : Optional.empty();
    }

    /** The same class name can sit on several contracts of one policy; block only when none of them is active. */
    private Optional<CoverageClass> findOnlyInactiveClass(Long policyId, String className) {
        if (!hasText(className)) {
            return Optional.empty();
        }
        List<CoverageClass> classes =
                coverageClassRepository.findByInsuranceCompanyPolicy_IdAndNameIgnoreCase(policyId, className.trim());
        boolean anyActive = classes.stream().anyMatch(coverageClass -> !isInactive(coverageClass.getIsActive()));
        return anyActive ? Optional.empty() : classes.stream().findFirst();
    }

    private static boolean isInactive(Boolean isActive) {
        return Boolean.FALSE.equals(isActive);
    }

    private static String payerName(NphiesPayer payer) {
        return hasText(payer.getNameEn()) ? payer.getNameEn().trim() : payer.getNphiesId();
    }

    private static String policyLabel(InsuranceCompanyPolicy policy) {
        return hasText(policy.getPolicyName())
                ? policy.getPolicyName().trim() + " (" + policy.getPolicyNumber() + ")"
                : policy.getPolicyNumber();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
