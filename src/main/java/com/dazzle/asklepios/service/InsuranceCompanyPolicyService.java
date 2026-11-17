package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.InsuranceCompanyPolicy;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.repository.InsuranceCompanyPolicyRepository;
import com.dazzle.asklepios.repository.InsuranceCompanyPolicySpecifications;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.insurancecompanypolicy.InsuranceCompanyPolicySaveVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InsuranceCompanyPolicyService {

    private static final Logger LOG = LoggerFactory.getLogger(InsuranceCompanyPolicyService.class);
    private static final String ENTITY_NAME = "insuranceCompanyPolicy";

    private final InsuranceCompanyPolicyRepository insuranceCompanyPolicyRepository;
    private final NphiesPayerRepository nphiesPayerRepository;

    public InsuranceCompanyPolicyService(
            InsuranceCompanyPolicyRepository insuranceCompanyPolicyRepository,
            NphiesPayerRepository nphiesPayerRepository
    ) {
        this.insuranceCompanyPolicyRepository = insuranceCompanyPolicyRepository;
        this.nphiesPayerRepository = nphiesPayerRepository;
    }

    public InsuranceCompanyPolicy create(Long insuranceCompanyId, InsuranceCompanyPolicySaveVM vm) {
        LOG.debug("Create insurance company policy insuranceCompanyId={} payload={}", insuranceCompanyId, vm);

        NphiesPayer insuranceCompany = requireInsuranceCompany(insuranceCompanyId);
        String policyCode = vm.policyCode().trim();
        String policyNumber = blankToNull(vm.policyNumber());
        validateValidity(vm);
        validateUniqueness(insuranceCompanyId, null, policyCode, policyNumber);

        InsuranceCompanyPolicy policy = InsuranceCompanyPolicy.builder()
                .insuranceCompany(insuranceCompany)
                .isActive(vm.isActive() == null || vm.isActive())
                .build();
        applyPolicy(policy, vm, policyCode, policyNumber);
        return save(policy);
    }

    public InsuranceCompanyPolicy update(Long insuranceCompanyId, Long policyId, InsuranceCompanyPolicySaveVM vm) {
        LOG.debug("Update insurance company policy insuranceCompanyId={} policyId={} payload={}", insuranceCompanyId, policyId, vm);
        InsuranceCompanyPolicy policy = requirePolicy(insuranceCompanyId, policyId);
        String policyCode = vm.policyCode().trim();
        String policyNumber = blankToNull(vm.policyNumber());
        validateValidity(vm);
        validateUniqueness(insuranceCompanyId, policyId, policyCode, policyNumber);

        applyPolicy(policy, vm, policyCode, policyNumber);
        if (vm.isActive() != null) {
            policy.setIsActive(vm.isActive());
        }
        return save(policy);
    }

    public InsuranceCompanyPolicy toggleActive(Long insuranceCompanyId, Long policyId, String reason) {
        LOG.debug("Toggle insurance company policy active insuranceCompanyId={} policyId={}", insuranceCompanyId, policyId);
        InsuranceCompanyPolicy policy = requirePolicy(insuranceCompanyId, policyId);
        boolean deactivating = Boolean.TRUE.equals(policy.getIsActive());
        policy.setInactiveReason(InactiveReason.forToggle(deactivating, reason, ENTITY_NAME, "policy"));
        policy.setIsActive(!deactivating);
        return insuranceCompanyPolicyRepository.save(policy);
    }

    @Transactional(readOnly = true)
    public List<InsuranceCompanyPolicy> search(
            Long insuranceCompanyId,
            String policyName,
            String policyNumber,
            String policyCode
    ) {
        LOG.debug(
                "Search insurance company policies insuranceCompanyId={} policyName={} policyNumber={} policyCode={}",
                insuranceCompanyId,
                policyName,
                policyNumber,
                policyCode
        );
        requireInsuranceCompany(insuranceCompanyId);
        return insuranceCompanyPolicyRepository.findAll(
                InsuranceCompanyPolicySpecifications.search(insuranceCompanyId, policyName, policyNumber, policyCode),
                Sort.by(Sort.Order.desc("validFrom"), Sort.Order.desc("id"))
        );
    }

    private void applyPolicy(
            InsuranceCompanyPolicy policy,
            InsuranceCompanyPolicySaveVM vm,
            String policyCode,
            String policyNumber
    ) {
        policy.setPolicyName(vm.policyName().trim());
        policy.setPolicyNumber(policyNumber);
        policy.setPolicyCode(policyCode);
        policy.setValidFrom(vm.validFrom());
        policy.setValidTo(vm.validTo());
    }

    private InsuranceCompanyPolicy save(InsuranceCompanyPolicy policy) {
        try {
            return insuranceCompanyPolicyRepository.save(policy);
        } catch (DataIntegrityViolationException exception) {
            throw new BadRequestAlertException(
                    "A policy with this code or number already exists for the insurance company.",
                    ENTITY_NAME,
                    "duplicate"
            );
        }
    }

    private InsuranceCompanyPolicy requirePolicy(Long insuranceCompanyId, Long policyId) {
        return insuranceCompanyPolicyRepository.findByIdAndInsuranceCompany_Id(policyId, insuranceCompanyId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Policy not found.",
                        ENTITY_NAME,
                        "notfound"
                ));
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private NphiesPayer requireInsuranceCompany(Long insuranceCompanyId) {
        return nphiesPayerRepository.findById(insuranceCompanyId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Insurance company not found.",
                        ENTITY_NAME,
                        "notfound"
                ));
    }

    private void validateValidity(InsuranceCompanyPolicySaveVM vm) {
        if (vm.validTo() != null && vm.validTo().isBefore(vm.validFrom())) {
            throw new BadRequestAlertException(
                    "Valid to must be on or after valid from.",
                    ENTITY_NAME,
                    "invalidvalidity"
            );
        }
    }

    private void validateUniqueness(Long insuranceCompanyId, Long policyId, String policyCode, String policyNumber) {
        boolean codeExists = policyId == null
                ? insuranceCompanyPolicyRepository.existsByInsuranceCompany_IdAndPolicyCodeIgnoreCase(insuranceCompanyId, policyCode)
                : insuranceCompanyPolicyRepository.existsByInsuranceCompany_IdAndPolicyCodeIgnoreCaseAndIdNot(insuranceCompanyId, policyCode, policyId);
        if (codeExists) {
            throw new BadRequestAlertException(
                    "A policy with this code already exists for the insurance company.",
                    ENTITY_NAME,
                    "policycode.exists"
            );
        }
        if (policyNumber == null) {
            return;
        }
        boolean numberExists = policyId == null
                ? insuranceCompanyPolicyRepository.existsByInsuranceCompany_IdAndPolicyNumberIgnoreCase(insuranceCompanyId, policyNumber)
                : insuranceCompanyPolicyRepository.existsByInsuranceCompany_IdAndPolicyNumberIgnoreCaseAndIdNot(insuranceCompanyId, policyNumber, policyId);
        if (numberExists) {
            throw new BadRequestAlertException(
                    "A policy with this number already exists for the insurance company.",
                    ENTITY_NAME,
                    "policynumber.exists"
            );
        }
    }
}
