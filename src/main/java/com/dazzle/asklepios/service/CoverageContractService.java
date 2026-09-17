package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.enumeration.CoverageClassName;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import com.dazzle.asklepios.repository.CoverageContractRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractResponseVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractSaveVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CoverageContractService {

    private static final Logger LOG = LoggerFactory.getLogger(CoverageContractService.class);
    private static final String ENTITY = "coverageContract";

    private final CoverageContractRepository coverageContractRepository;
    private final CoverageLookupService coverageLookupService;
    private final NphiesPayerRepository nphiesPayerRepository;
    private final PriceListSetupRepository priceListSetupRepository;

    public CoverageContractService(
            CoverageContractRepository coverageContractRepository,
            CoverageLookupService coverageLookupService,
            NphiesPayerRepository nphiesPayerRepository,
            PriceListSetupRepository priceListSetupRepository
    ) {
        this.coverageContractRepository = coverageContractRepository;
        this.coverageLookupService = coverageLookupService;
        this.nphiesPayerRepository = nphiesPayerRepository;
        this.priceListSetupRepository = priceListSetupRepository;
    }

    public CoverageContractResponseVM create(CoverageContractSaveVM vm) {
        LOG.debug("Create coverage contract payload={}", vm);
        validateCompany(vm.guarantorType(), vm.companyId());
        PriceListSetup priceList = coverageLookupService.requireInsurancePriceList(vm.priceListSetupId());
        NphiesPayer insurancePayer = resolveInsurancePayer(vm.guarantorType(), vm.companyId(), vm.insurancePayerId(), priceList);
        NphiesPayer parentPayer = resolveParentPayer(vm.parentPayerId());
        ensureUniqueCode(vm.guarantorType(), vm.companyId(), vm.code(), null);

        CoverageContract saved = coverageContractRepository.save(CoverageContract.builder()
                .guarantorType(vm.guarantorType())
                .companyId(vm.companyId())
                .code(vm.code().trim())
                .policyNumber(vm.policyNumber().trim())
                .coverageBasis(vm.coverageBasis())
                .insurancePayerId(insurancePayer.getId())
                .priceListSetupId(priceList.getId())
                .parentPayerId(parentPayer != null ? parentPayer.getId() : null)
                .className(vm.className())
                .approvalCoverageCompany(vm.approvalCoverageCompany())
                .isActive(vm.isActive() != null ? vm.isActive() : Boolean.TRUE)
                .build());
        return toResponse(saved);
    }

    public CoverageContractResponseVM update(CoverageContractUpdateVM vm) {
        LOG.debug("Update coverage contract payload={}", vm);
        CoverageContract existing = getEntity(vm.id());
        validateCompany(vm.guarantorType(), vm.companyId());
        PriceListSetup priceList = coverageLookupService.requireInsurancePriceList(vm.priceListSetupId());
        NphiesPayer insurancePayer = resolveInsurancePayer(vm.guarantorType(), vm.companyId(), vm.insurancePayerId(), priceList);
        NphiesPayer parentPayer = resolveParentPayer(vm.parentPayerId());
        ensureUniqueCode(vm.guarantorType(), vm.companyId(), vm.code(), existing.getId());

        existing.setGuarantorType(vm.guarantorType());
        existing.setCompanyId(vm.companyId());
        existing.setCode(vm.code().trim());
        existing.setPolicyNumber(vm.policyNumber().trim());
        existing.setCoverageBasis(vm.coverageBasis());
        existing.setInsurancePayerId(insurancePayer.getId());
        existing.setPriceListSetupId(priceList.getId());
        existing.setParentPayerId(parentPayer != null ? parentPayer.getId() : null);
        existing.setClassName(vm.className());
        existing.setApprovalCoverageCompany(vm.approvalCoverageCompany());
        if (vm.isActive() != null) {
            existing.setIsActive(vm.isActive());
        }
        return toResponse(coverageContractRepository.save(existing));
    }

    public CoverageContractResponseVM toggleActive(Long id) {
        CoverageContract existing = getEntity(id);
        existing.setIsActive(!Boolean.TRUE.equals(existing.getIsActive()));
        return toResponse(coverageContractRepository.save(existing));
    }

    @Transactional(readOnly = true)
    public CoverageContractResponseVM get(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public CoverageContract getEntity(Long id) {
        return coverageContractRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Coverage contract was not found.", ENTITY, "notFound"));
    }

    @Transactional(readOnly = true)
    public Page<CoverageContractResponseVM> search(
            GuarantorType guarantorType,
            Long companyId,
            Long insurancePayerId,
            Boolean isActive,
            CoverageClassName className,
            String search,
            Pageable pageable
    ) {
        String q = search == null || search.isBlank() ? null : search.trim();
        return coverageContractRepository.search(guarantorType, companyId, insurancePayerId, isActive, className, q, pageable)
                .map(this::toResponse);
    }

    private void validateCompany(GuarantorType guarantorType, Long companyId) {
        if (guarantorType == GuarantorType.TPA) {
            coverageLookupService.requireActiveTpa(companyId);
            return;
        }
        if (guarantorType == GuarantorType.INSURANCE) {
            coverageLookupService.requireActivePayer(companyId, "Insurance company");
            return;
        }
        throw new BadRequestAlertException("Guarantor type must be Insurance or TPA.", ENTITY, "invalidGuarantorType");
    }

    private NphiesPayer resolveInsurancePayer(
            GuarantorType guarantorType,
            Long companyId,
            Long insurancePayerId,
            PriceListSetup priceList
    ) {
        Long listPayerId = priceList.getNphiesPayerId() != null
                ? priceList.getNphiesPayerId()
                : priceList.getPayerId();
        if (listPayerId == null) {
            throw new BadRequestAlertException("Selected price list is not linked to an insurance company.", ENTITY, "priceListNotLinked");
        }
        if (guarantorType == GuarantorType.INSURANCE) {
            if (!companyId.equals(listPayerId)) {
                throw new BadRequestAlertException("Selected price list does not belong to the selected insurance company.", ENTITY, "priceListMismatch");
            }
            return coverageLookupService.requireActivePayer(companyId, "Insurance company");
        }
        if (guarantorType == GuarantorType.TPA) {
            if (insurancePayerId == null) {
                throw new BadRequestAlertException("Insurance name is required.", ENTITY, "insuranceRequired");
            }
            if (!coverageLookupService.isPayerLinkedToTpa(companyId, insurancePayerId)) {
                throw new BadRequestAlertException("Selected insurance is not linked to this TPA.", ENTITY, "priceListMismatch");
            }
            if (listPayerId == null || !insurancePayerId.equals(listPayerId)) {
                throw new BadRequestAlertException("Selected price list does not belong to the selected insurance name.", ENTITY, "priceListMismatch");
            }
            return coverageLookupService.requireActivePayer(insurancePayerId, "Insurance name");
        }
        throw new BadRequestAlertException("Guarantor type must be Insurance or TPA.", ENTITY, "invalidGuarantorType");
    }

    private NphiesPayer resolveParentPayer(Long parentPayerId) {
        if (parentPayerId == null) {
            return null;
        }
        return coverageLookupService.requireActivePayer(parentPayerId, "Parent name");
    }

    private void ensureUniqueCode(GuarantorType guarantorType, Long companyId, String code, Long id) {
        boolean exists = id == null
                ? coverageContractRepository.existsByGuarantorTypeAndCompanyIdAndCodeIgnoreCase(guarantorType, companyId, code)
                : coverageContractRepository.existsByGuarantorTypeAndCompanyIdAndCodeIgnoreCaseAndIdNot(guarantorType, companyId, code, id);
        if (exists) {
            throw new BadRequestAlertException("Coverage code already exists for this company.", ENTITY, "codeExists");
        }
    }

    public CoverageContractResponseVM toResponse(CoverageContract contract) {
        PriceListSetup priceList = priceListSetupRepository.findById(contract.getPriceListSetupId()).orElse(null);
        NphiesPayer insurancePayer = nphiesPayerRepository.findById(contract.getInsurancePayerId()).orElse(null);
        NphiesPayer parentPayer = contract.getParentPayerId() == null
                ? null
                : nphiesPayerRepository.findById(contract.getParentPayerId()).orElse(null);

        return new CoverageContractResponseVM(
                contract.getId(),
                contract.getGuarantorType(),
                contract.getCompanyId(),
                coverageLookupService.companyName(contract.getGuarantorType(), contract.getCompanyId()),
                coverageLookupService.companyCode(contract.getGuarantorType(), contract.getCompanyId()),
                contract.getCode(),
                contract.getPolicyNumber(),
                contract.getCoverageBasis(),
                contract.getInsurancePayerId(),
                insurancePayer == null ? null : coverageLookupService.payerDisplayName(insurancePayer),
                contract.getPriceListSetupId(),
                priceList == null ? null : priceList.getName(),
                priceList == null ? null : priceList.getEffectiveFrom(),
                priceList == null ? null : priceList.getEffectiveTo(),
                contract.getParentPayerId(),
                parentPayer == null ? null : coverageLookupService.payerDisplayName(parentPayer),
                contract.getClassName(),
                contract.getApprovalCoverageCompany(),
                contract.getIsActive(),
                contract.getCreatedBy(),
                contract.getCreatedDate(),
                contract.getLastModifiedBy(),
                contract.getLastModifiedDate()
        );
    }
}
