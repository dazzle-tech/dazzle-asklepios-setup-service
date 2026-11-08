package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageClass;
import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import com.dazzle.asklepios.repository.CoverageContractRepository;
import com.dazzle.asklepios.repository.CoverageContractSpecifications;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        Boolean isActive = vm.isActive() != null ? vm.isActive() : Boolean.TRUE;
        Long parentPayerId = resolveParentPayerId(vm.parentPayerId(), insurancePayer.getId());
        ensureSingleActivePeriod(insurancePayer.getId(), priceList, isActive, null);

        CoverageContract saved = coverageContractRepository.save(CoverageContract.builder()
                .guarantorType(vm.guarantorType())
                .companyId(vm.companyId())
                .coverageBasis(vm.coverageBasis())
                .insurancePayerId(insurancePayer.getId())
                .priceListSetupId(priceList.getId())
                .parentPayerId(parentPayerId)
                .approvalCoverageCompany(vm.approvalCoverageCompany())
                .isActive(isActive)
                .build());
        return toResponse(saved);
    }

    public CoverageContractResponseVM update(CoverageContractUpdateVM vm) {
        LOG.debug("Update coverage contract payload={}", vm);
        CoverageContract existing = getEntity(vm.id());
        validateCompany(vm.guarantorType(), vm.companyId());
        PriceListSetup priceList = coverageLookupService.requireInsurancePriceList(vm.priceListSetupId());
        NphiesPayer insurancePayer = resolveInsurancePayer(vm.guarantorType(), vm.companyId(), vm.insurancePayerId(), priceList);
        Boolean isActive = vm.isActive() != null ? vm.isActive() : existing.getIsActive();
        Long parentPayerId = resolveParentPayerId(vm.parentPayerId(), insurancePayer.getId());
        ensureSingleActivePeriod(insurancePayer.getId(), priceList, isActive, existing.getId());

        existing.setGuarantorType(vm.guarantorType());
        existing.setCompanyId(vm.companyId());
        existing.setCoverageBasis(vm.coverageBasis());
        existing.setInsurancePayerId(insurancePayer.getId());
        existing.setPriceListSetupId(priceList.getId());
        existing.setParentPayerId(parentPayerId);
        existing.setApprovalCoverageCompany(vm.approvalCoverageCompany());
        existing.setIsActive(isActive);
        return toResponse(coverageContractRepository.save(existing));
    }

    public CoverageContractResponseVM toggleActive(Long id) {
        CoverageContract existing = getEntity(id);
        boolean nextActive = !Boolean.TRUE.equals(existing.getIsActive());
        if (nextActive) {
            PriceListSetup priceList = priceListSetupRepository.findById(existing.getPriceListSetupId()).orElse(null);
            ensureSingleActivePeriod(existing.getInsurancePayerId(), priceList, true, existing.getId());
        }
        existing.setIsActive(nextActive);
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
            String search,
            Pageable pageable
    ) {
        String q = search == null || search.isBlank() ? null : search.trim();
        return coverageContractRepository
                .findAll(
                        CoverageContractSpecifications.search(guarantorType, companyId, insurancePayerId, isActive, q),
                        pageable
                )
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
            if (!insurancePayerId.equals(listPayerId)) {
                throw new BadRequestAlertException("Selected price list does not belong to the selected insurance name.", ENTITY, "priceListMismatch");
            }
            return coverageLookupService.requireActivePayer(insurancePayerId, "Insurance name");
        }
        throw new BadRequestAlertException("Guarantor type must be Insurance or TPA.", ENTITY, "invalidGuarantorType");
    }

    private Long resolveParentPayerId(Long parentPayerId, Long insurancePayerId) {
        if (parentPayerId == null) {
            return null;
        }
        if (parentPayerId.equals(insurancePayerId)) {
            throw new BadRequestAlertException(
                    "Parent insurance cannot be the same insurance company.",
                    ENTITY,
                    "parentSameAsInsurance"
            );
        }
        return coverageLookupService.requireActivePayer(parentPayerId, "Parent insurance").getId();
    }

    private void ensureSingleActivePeriod(
            Long insurancePayerId,
            PriceListSetup priceList,
            Boolean isActive,
            Long currentId
    ) {
        if (!Boolean.TRUE.equals(isActive) || insurancePayerId == null) {
            return;
        }
        List<CoverageContract> activeContracts = coverageContractRepository
                .findByInsurancePayerIdAndIsActiveTrue(insurancePayerId);
        if (activeContracts.isEmpty()) {
            return;
        }

        Set<Long> priceListIds = new HashSet<>();
        if (priceList != null && priceList.getId() != null) {
            priceListIds.add(priceList.getId());
        }
        for (CoverageContract other : activeContracts) {
            if (other.getPriceListSetupId() != null) {
                priceListIds.add(other.getPriceListSetupId());
            }
        }
        Map<Long, PriceListSetup> priceListsById = new HashMap<>();
        if (!priceListIds.isEmpty()) {
            for (PriceListSetup list : priceListSetupRepository.findAllById(priceListIds)) {
                priceListsById.put(list.getId(), list);
            }
        }
        if (priceList != null && priceList.getId() != null) {
            priceListsById.put(priceList.getId(), priceList);
        }

        LocalDate from = priceList == null ? null : priceList.getEffectiveFrom();
        LocalDate to = priceList == null ? null : priceList.getEffectiveTo();
        for (CoverageContract other : activeContracts) {
            if (currentId != null && currentId.equals(other.getId())) {
                continue;
            }
            PriceListSetup otherList = priceListsById.get(other.getPriceListSetupId());
            LocalDate otherFrom = otherList == null ? null : otherList.getEffectiveFrom();
            LocalDate otherTo = otherList == null ? null : otherList.getEffectiveTo();
            if (periodsOverlap(from, to, otherFrom, otherTo)) {
                throw new BadRequestAlertException(
                        "This insurance company already has an active contract for this period. Only one active contract is allowed for the same dates.",
                        ENTITY,
                        "activeContractPeriodOverlap"
                );
            }
        }
    }

    private boolean periodsOverlap(LocalDate from, LocalDate to, LocalDate otherFrom, LocalDate otherTo) {
        LocalDate start = from == null ? LocalDate.MIN : from;
        LocalDate end = to == null ? LocalDate.MAX : to;
        LocalDate otherStart = otherFrom == null ? LocalDate.MIN : otherFrom;
        LocalDate otherEnd = otherTo == null ? LocalDate.MAX : otherTo;
        return !start.isAfter(otherEnd) && !otherStart.isAfter(end);
    }

    private String formatParentName(NphiesPayer parentPayer) {
        String code = parentPayer.getNphiesId() == null ? "" : parentPayer.getNphiesId().trim();
        String name = coverageLookupService.payerDisplayName(parentPayer);
        if (!code.isBlank() && name != null && !name.isBlank()) {
            return code + " — " + name;
        }
        if (name != null && !name.isBlank()) {
            return name;
        }
        return code.isBlank() ? null : code;
    }

    public CoverageContractResponseVM toResponse(CoverageContract contract) {
        return toResponse(contract, null);
    }

    public CoverageContractResponseVM toResponse(CoverageContract contract, CoverageClass coverageClass) {
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
                contract.getCoverageBasis(),
                contract.getInsurancePayerId(),
                insurancePayer == null ? null : coverageLookupService.payerDisplayName(insurancePayer),
                contract.getPriceListSetupId(),
                priceList == null ? null : priceList.getName(),
                priceList == null ? null : priceList.getEffectiveFrom(),
                priceList == null ? null : priceList.getEffectiveTo(),
                parentPayer == null ? null : parentPayer.getId(),
                parentPayer == null ? null : formatParentName(parentPayer),
                coverageClass == null ? null : coverageClass.getId(),
                coverageClass == null ? null : coverageClass.getName(),
                contract.getApprovalCoverageCompany(),
                contract.getIsActive(),
                contract.getCreatedBy(),
                contract.getCreatedDate(),
                contract.getLastModifiedBy(),
                contract.getLastModifiedDate()
        );
    }
}
