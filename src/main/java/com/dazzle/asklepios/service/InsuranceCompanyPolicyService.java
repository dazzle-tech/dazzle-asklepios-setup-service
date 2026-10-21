package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.InsuranceCompanyPolicy;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.repository.InsuranceCompanyPolicyRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.insurancecompanypolicy.InsuranceCompanyPolicySaveVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
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
        String policyName = vm.policyName().trim();
        String policyNumber = vm.policyNumber().trim();
        String policyCode = vm.policyCode().trim();
        validateValidity(vm);
        validateUniqueness(insuranceCompanyId, policyCode, policyNumber);

        InsuranceCompanyPolicy policy = InsuranceCompanyPolicy.builder()
                .insuranceCompany(insuranceCompany)
                .policyName(policyName)
                .policyNumber(policyNumber)
                .policyCode(policyCode)
                .validFrom(vm.validFrom())
                .validTo(vm.validTo())
                .build();

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

    @Transactional(readOnly = true)
    public List<InsuranceCompanyPolicy> findByInsuranceCompanyId(Long insuranceCompanyId) {
        LOG.debug("List insurance company policies insuranceCompanyId={}", insuranceCompanyId);
        requireInsuranceCompany(insuranceCompanyId);
        return insuranceCompanyPolicyRepository.findByInsuranceCompany_IdOrderByValidFromDescIdDesc(insuranceCompanyId);
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
        if (vm.validTo().isBefore(vm.validFrom())) {
            throw new BadRequestAlertException(
                    "Valid to must be on or after valid from.",
                    ENTITY_NAME,
                    "invalidvalidity"
            );
        }
    }

    private void validateUniqueness(Long insuranceCompanyId, String policyCode, String policyNumber) {
        if (insuranceCompanyPolicyRepository.existsByInsuranceCompany_IdAndPolicyCodeIgnoreCase(insuranceCompanyId, policyCode)) {
            throw new BadRequestAlertException(
                    "A policy with this code already exists for the insurance company.",
                    ENTITY_NAME,
                    "policycode.exists"
            );
        }
        if (insuranceCompanyPolicyRepository.existsByInsuranceCompany_IdAndPolicyNumberIgnoreCase(insuranceCompanyId, policyNumber)) {
            throw new BadRequestAlertException(
                    "A policy with this number already exists for the insurance company.",
                    ENTITY_NAME,
                    "policynumber.exists"
            );
        }
    }
}
