package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageClass;
import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.InsuranceCompanyPolicy;
import com.dazzle.asklepios.repository.CoverageClassRepository;
import com.dazzle.asklepios.repository.InsuranceCompanyPolicyRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageClassResponseVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageClassSaveVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageClassUpdateVM;
import com.dazzle.asklepios.web.rest.vm.coverage.PolicyCoverageClassResponseVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CoverageClassService {

    private static final Logger LOG = LoggerFactory.getLogger(CoverageClassService.class);
    private static final String ENTITY = "coverageClass";

    private final CoverageClassRepository coverageClassRepository;
    private final CoverageContractService coverageContractService;
    private final InsuranceCompanyPolicyRepository insuranceCompanyPolicyRepository;
    private final CoverageLookupService coverageLookupService;

    public CoverageClassService(
            CoverageClassRepository coverageClassRepository,
            CoverageContractService coverageContractService,
            InsuranceCompanyPolicyRepository insuranceCompanyPolicyRepository,
            CoverageLookupService coverageLookupService
    ) {
        this.coverageClassRepository = coverageClassRepository;
        this.coverageContractService = coverageContractService;
        this.insuranceCompanyPolicyRepository = insuranceCompanyPolicyRepository;
        this.coverageLookupService = coverageLookupService;
    }

    public CoverageClassResponseVM create(CoverageClassSaveVM vm) {
        LOG.debug("Create coverage class payload={}", vm);
        CoverageContract contract = coverageContractService.getEntity(vm.coverageContractId());
        String name = requireName(vm.name());
        ensureUniqueName(contract.getId(), name, null);

        CoverageClass saved = coverageClassRepository.save(CoverageClass.builder()
                .coverageContract(contract)
                .name(name)
                .isActive(vm.isActive() != null ? vm.isActive() : Boolean.TRUE)
                .build());
        return CoverageClassResponseVM.of(saved);
    }

    public CoverageClassResponseVM update(CoverageClassUpdateVM vm) {
        LOG.debug("Update coverage class payload={}", vm);
        CoverageClass existing = getEntity(vm.id());
        String name = requireName(vm.name());
        ensureUniqueName(existing.getCoverageContract().getId(), name, existing.getId());
        existing.setName(name);
        if (vm.isActive() != null) {
            existing.setIsActive(vm.isActive());
        }
        return CoverageClassResponseVM.of(coverageClassRepository.save(existing));
    }

    public CoverageClassResponseVM toggleActive(Long id) {
        CoverageClass existing = getEntity(id);
        existing.setIsActive(!Boolean.TRUE.equals(existing.getIsActive()));
        return CoverageClassResponseVM.of(coverageClassRepository.save(existing));
    }

    public CoverageClassResponseVM linkPolicy(Long classId, Long insuranceCompanyPolicyId) {
        LOG.debug("Link coverage class id={} to policy id={}", classId, insuranceCompanyPolicyId);
        CoverageClass coverageClass = getEntity(classId);
        CoverageContract contract = coverageClass.getCoverageContract();
        InsuranceCompanyPolicy policy = insuranceCompanyPolicyRepository.findById(insuranceCompanyPolicyId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Policy was not found.",
                        ENTITY,
                        "policyNotFound"
                ));

        Long insuranceCompanyId = policy.getInsuranceCompany() == null ? null : policy.getInsuranceCompany().getId();
        if (contract.getInsurancePayerId() == null || !contract.getInsurancePayerId().equals(insuranceCompanyId)) {
            throw new BadRequestAlertException(
                    "This policy does not belong to the contract insurance company.",
                    ENTITY,
                    "policyCompanyMismatch"
            );
        }

        coverageClass.setInsuranceCompanyPolicy(policy);
        return CoverageClassResponseVM.of(coverageClassRepository.save(coverageClass));
    }

    @Transactional(readOnly = true)
    public CoverageClassResponseVM get(Long id) {
        return CoverageClassResponseVM.of(getEntity(id));
    }

    @Transactional(readOnly = true)
    public CoverageClass getEntity(Long id) {
        return coverageClassRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Coverage class was not found.", ENTITY, "notFound"));
    }

    @Transactional(readOnly = true)
    public List<PolicyCoverageClassResponseVM> listByPolicy(Long insuranceCompanyId, Long policyId) {
        LOG.debug("List coverage classes for policy insuranceCompanyId={} policyId={}", insuranceCompanyId, policyId);
        if (!insuranceCompanyPolicyRepository.existsByIdAndInsuranceCompany_Id(policyId, insuranceCompanyId)) {
            throw new BadRequestAlertException("Policy was not found.", ENTITY, "policyNotFound");
        }

        Map<Long, String> coverageNames = new HashMap<>();
        return coverageClassRepository.findByInsuranceCompanyPolicy_IdOrderByNameAscIdAsc(policyId)
                .stream()
                .map(coverageClass -> toPolicyClass(coverageClass, coverageNames))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CoverageClassResponseVM> list(Long contractId, Boolean isActive, Pageable pageable) {
        coverageContractService.getEntity(contractId);
        Page<CoverageClass> page = isActive == null
                ? coverageClassRepository.findByCoverageContract_Id(contractId, pageable)
                : coverageClassRepository.findByCoverageContract_IdAndIsActive(contractId, isActive, pageable);
        return page.map(CoverageClassResponseVM::of);
    }

    private PolicyCoverageClassResponseVM toPolicyClass(CoverageClass coverageClass, Map<Long, String> coverageNames) {
        CoverageContract contract = coverageClass.getCoverageContract();
        if (contract == null || contract.getId() == null) {
            return new PolicyCoverageClassResponseVM(
                    coverageClass.getId(),
                    coverageClass.getName(),
                    coverageClass.getIsActive(),
                    null,
                    null,
                    null
            );
        }
        if (!coverageNames.containsKey(contract.getId())) {
            coverageNames.put(
                    contract.getId(),
                    coverageLookupService.companyName(contract.getGuarantorType(), contract.getCompanyId())
            );
        }
        String coverageName = coverageNames.get(contract.getId());
        return new PolicyCoverageClassResponseVM(
                coverageClass.getId(),
                coverageClass.getName(),
                coverageClass.getIsActive(),
                contract.getId(),
                contract.getGuarantorType(),
                coverageName
        );
    }

    private String requireName(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestAlertException("Class name is required.", ENTITY, "nameRequired");
        }
        return raw.trim();
    }

    private void ensureUniqueName(Long contractId, String name, Long id) {
        boolean exists = id == null
                ? coverageClassRepository.existsByCoverageContract_IdAndNameIgnoreCase(contractId, name)
                : coverageClassRepository.existsByCoverageContract_IdAndNameIgnoreCaseAndIdNot(contractId, name, id);
        if (exists) {
            throw new BadRequestAlertException("Class name already exists under this coverage header.", ENTITY, "nameExists");
        }
    }
}
