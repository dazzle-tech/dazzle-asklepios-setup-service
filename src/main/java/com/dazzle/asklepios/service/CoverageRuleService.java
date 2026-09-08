package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.CoverageCopayment;
import com.dazzle.asklepios.domain.CoverageDiscount;
import com.dazzle.asklepios.domain.CoverageExclusion;
import com.dazzle.asklepios.domain.CoveragePreApproval;
import com.dazzle.asklepios.domain.CoveragePreApprovalItem;
import com.dazzle.asklepios.domain.CoverageTerm;
import com.dazzle.asklepios.domain.CoverageTermItem;
import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.ICDDiagnosis;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.CoverageApprovalScope;
import com.dazzle.asklepios.domain.enumeration.CoverageDiagnosisScope;
import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.CoverageTermType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.domain.enumeration.patient.YesNoQuestion;
import com.dazzle.asklepios.repository.CoverageCopaymentRepository;
import com.dazzle.asklepios.repository.CoverageDiscountRepository;
import com.dazzle.asklepios.repository.CoverageExclusionRepository;
import com.dazzle.asklepios.repository.CoveragePreApprovalItemRepository;
import com.dazzle.asklepios.repository.CoveragePreApprovalRepository;
import com.dazzle.asklepios.repository.CoverageTermItemRepository;
import com.dazzle.asklepios.repository.CoverageTermRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageCopaymentVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageDiscountVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageExclusionVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoveragePreApprovalItemVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoveragePreApprovalVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageTermItemVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageTermVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class CoverageRuleService {

    private static final String ENTITY = "coverageContract";

    private final CoverageContractService coverageContractService;
    private final CoverageLookupService coverageLookupService;
    private final CoverageCopaymentRepository copaymentRepository;
    private final CoverageTermRepository termRepository;
    private final CoverageTermItemRepository termItemRepository;
    private final CoverageDiscountRepository discountRepository;
    private final CoverageExclusionRepository exclusionRepository;
    private final CoveragePreApprovalRepository preApprovalRepository;
    private final CoveragePreApprovalItemRepository preApprovalItemRepository;

    public CoverageRuleService(
            CoverageContractService coverageContractService,
            CoverageLookupService coverageLookupService,
            CoverageCopaymentRepository copaymentRepository,
            CoverageTermRepository termRepository,
            CoverageTermItemRepository termItemRepository,
            CoverageDiscountRepository discountRepository,
            CoverageExclusionRepository exclusionRepository,
            CoveragePreApprovalRepository preApprovalRepository,
            CoveragePreApprovalItemRepository preApprovalItemRepository
    ) {
        this.coverageContractService = coverageContractService;
        this.coverageLookupService = coverageLookupService;
        this.copaymentRepository = copaymentRepository;
        this.termRepository = termRepository;
        this.termItemRepository = termItemRepository;
        this.discountRepository = discountRepository;
        this.exclusionRepository = exclusionRepository;
        this.preApprovalRepository = preApprovalRepository;
        this.preApprovalItemRepository = preApprovalItemRepository;
    }

    public CoverageCopaymentVM saveCopayment(Long contractId, CoverageCopaymentVM vm) {
        CoverageContract contract = coverageContractService.getEntity(contractId);
        requirePositive(vm.valueAmount(), "Co-payment value");
        CoverageCopayment entity = vm.id() == null
                ? CoverageCopayment.builder().coverageContract(contract).isActive(true).build()
                : requireOwnedCopayment(vm.id(), contractId);
        entity.setEncounterType(vm.encounterType());
        entity.setValueType(vm.valueType());
        entity.setValueAmount(vm.valueAmount());
        entity.setDiscountOnExcluded(Boolean.TRUE.equals(vm.discountOnExcluded()));
        entity.setDiscountOnCash(Boolean.TRUE.equals(vm.discountOnCash()));
        entity.setDiscountOnExceededCash(Boolean.TRUE.equals(vm.discountOnExceededCash()));
        if (vm.id() == null) {
            entity.setIsActive(true);
        } else if (vm.isActive() != null) {
            entity.setIsActive(vm.isActive());
        }
        return CoverageCopaymentVM.ofEntity(copaymentRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<CoverageCopaymentVM> listCopayments(Long contractId, Boolean isActive, Pageable pageable) {
        coverageContractService.getEntity(contractId);
        Page<CoverageCopayment> page = isActive == null
                ? copaymentRepository.findByCoverageContract_Id(contractId, pageable)
                : copaymentRepository.findByCoverageContract_IdAndIsActive(contractId, isActive, pageable);
        return page.map(CoverageCopaymentVM::ofEntity);
    }

    public CoverageTermVM saveTerm(Long contractId, CoverageTermVM vm) {
        CoverageContract contract = coverageContractService.getEntity(contractId);
        validateTerm(vm);
        CoverageTerm entity = vm.id() == null
                ? CoverageTerm.builder().coverageContract(contract).isActive(true).build()
                : requireOwnedTerm(vm.id(), contractId);
        applyTerm(entity, vm);
        if (vm.id() == null) {
            entity.setIsActive(true);
        } else if (vm.isActive() != null) {
            entity.setIsActive(vm.isActive());
        }
        return toTermVm(termRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<CoverageTermVM> listTerms(Long contractId, CoverageTermType termType, Boolean isActive, Pageable pageable) {
        coverageContractService.getEntity(contractId);
        if (termType == null) {
            throw new BadRequestAlertException("Term type is required.", ENTITY, "termTypeRequired");
        }
        Page<CoverageTerm> page = isActive == null
                ? termRepository.findByCoverageContract_IdAndTermType(contractId, termType, pageable)
                : termRepository.findByCoverageContract_IdAndTermTypeAndIsActive(contractId, termType, isActive, pageable);
        return page.map(this::toTermVm);
    }

    public CoverageTermItemVM saveTermItem(Long termId, CoverageTermItemVM vm) {
        CoverageTerm term = termRepository.findById(termId)
                .orElseThrow(() -> new BadRequestAlertException("Coverage term was not found.", ENTITY, "notFound"));
        validateTermItem(vm);
        CoverageTermItem entity = vm.id() == null
                ? CoverageTermItem.builder().coverageTerm(term).isActive(true).build()
                : requireOwnedTermItem(vm.id(), termId);
        applyTermItem(entity, vm);
        if (vm.id() == null) {
            entity.setIsActive(true);
        } else if (vm.isActive() != null) {
            entity.setIsActive(vm.isActive());
        }
        return toTermItemVm(termItemRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<CoverageTermItemVM> listTermItems(Long termId, Boolean isActive, Pageable pageable) {
        requireTerm(termId);
        Page<CoverageTermItem> page = isActive == null
                ? termItemRepository.findByCoverageTerm_Id(termId, pageable)
                : termItemRepository.findByCoverageTerm_IdAndIsActive(termId, isActive, pageable);
        return page.map(this::toTermItemVm);
    }

    public CoverageDiscountVM createDiscount(Long contractId, CoverageDiscountVM vm) {
        CoverageContract contract = coverageContractService.getEntity(contractId);
        validateDiscount(vm);
        CoverageDiscount entity = CoverageDiscount.builder()
                .coverageContract(contract)
                .targetType(vm.targetType())
                .serviceCategory(vm.serviceCategory())
                .serviceId(resolveServiceId(vm.targetType(), vm.serviceId()))
                .encounterType(vm.encounterType())
                .discountType(vm.discountType())
                .discountValue(vm.discountValue())
                .isActive(true)
                .build();
        return toDiscountVm(discountRepository.save(entity));
    }

    public CoverageDiscountVM deactivateDiscount(Long id) {
        CoverageDiscount entity = discountRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Discount was not found.", ENTITY, "notFound"));
        deactivateOnly(entity.getIsActive());
        entity.setIsActive(false);
        return toDiscountVm(discountRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<CoverageDiscountVM> listDiscounts(Long contractId, Boolean isActive, Pageable pageable) {
        coverageContractService.getEntity(contractId);
        Page<CoverageDiscount> page = isActive == null
                ? discountRepository.findByCoverageContract_Id(contractId, pageable)
                : discountRepository.findByCoverageContract_IdAndIsActive(contractId, isActive, pageable);
        return page.map(this::toDiscountVm);
    }

    public CoverageExclusionVM createExclusion(Long contractId, CoverageExclusionVM vm) {
        CoverageContract contract = coverageContractService.getEntity(contractId);
        validateExclusion(vm);
        CoverageExclusion entity = CoverageExclusion.builder()
                .coverageContract(contract)
                .exclusionType(vm.exclusionType())
                .serviceCategory(vm.serviceCategory())
                .serviceId(vm.exclusionType() == CoverageRuleTarget.SERVICE ? requireService(vm.serviceId()).getId() : null)
                .allDiagnoses(Boolean.TRUE.equals(vm.allDiagnoses()) || vm.exclusionType() == CoverageRuleTarget.DIAGNOSIS && vm.diagnosisId() == null)
                .diagnosisId(resolveDiagnosisId(vm.exclusionType() == CoverageRuleTarget.DIAGNOSIS, vm.allDiagnoses(), vm.diagnosisId(), vm.diagnosisCode()))
                .encounterType(vm.encounterType())
                .excludedResult(vm.excludedResult() != null ? vm.excludedResult() : YesNoQuestion.YES)
                .isActive(true)
                .build();
        return toExclusionVm(exclusionRepository.save(entity));
    }

    public CoverageExclusionVM deactivateExclusion(Long id) {
        CoverageExclusion entity = exclusionRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Exclusion was not found.", ENTITY, "notFound"));
        deactivateOnly(entity.getIsActive());
        entity.setIsActive(false);
        return toExclusionVm(exclusionRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<CoverageExclusionVM> listExclusions(Long contractId, Boolean isActive, Pageable pageable) {
        coverageContractService.getEntity(contractId);
        Page<CoverageExclusion> page = isActive == null
                ? exclusionRepository.findByCoverageContract_Id(contractId, pageable)
                : exclusionRepository.findByCoverageContract_IdAndIsActive(contractId, isActive, pageable);
        return page.map(this::toExclusionVm);
    }

    public CoveragePreApprovalVM savePreApproval(Long contractId, CoveragePreApprovalVM vm) {
        CoverageContract contract = coverageContractService.getEntity(contractId);
        validatePreApproval(vm);
        CoveragePreApproval entity = vm.id() == null
                ? CoveragePreApproval.builder().coverageContract(contract).isActive(true).build()
                : requireOwnedPreApproval(vm.id(), contractId);
        entity.setApprovalScope(vm.approvalScope());
        entity.setFacilityId(vm.approvalScope() == CoverageApprovalScope.FACILITY ? requireFacility(vm.facilityId()).getId() : null);
        entity.setDepartmentId(vm.approvalScope() == CoverageApprovalScope.DEPARTMENT
                ? requireDepartment(vm.departmentId(), null).getId()
                : null);
        entity.setEncounterType(vm.approvalScope() == CoverageApprovalScope.FACILITY ? requireEncounterType(vm.encounterType()) : null);
        if (vm.id() == null) {
            entity.setIsActive(true);
        } else if (vm.isActive() != null) {
            entity.setIsActive(vm.isActive());
        }
        return toPreApprovalVm(preApprovalRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<CoveragePreApprovalVM> listPreApprovals(Long contractId, Boolean isActive, Pageable pageable) {
        coverageContractService.getEntity(contractId);
        Page<CoveragePreApproval> page = isActive == null
                ? preApprovalRepository.findByCoverageContract_Id(contractId, pageable)
                : preApprovalRepository.findByCoverageContract_IdAndIsActive(contractId, isActive, pageable);
        return page.map(this::toPreApprovalVm);
    }

    public CoveragePreApprovalItemVM createPreApprovalItem(Long preApprovalId, CoveragePreApprovalItemVM vm) {
        CoveragePreApproval parent = requirePreApproval(preApprovalId);
        if (!Boolean.TRUE.equals(parent.getIsActive())) {
            throw new BadRequestAlertException("Pre-approval must be active to add items.", ENTITY, "inactiveParent");
        }
        validatePreApprovalItem(vm);
        CoveragePreApprovalItem entity = CoveragePreApprovalItem.builder()
                .preApproval(parent)
                .itemType(vm.itemType())
                .serviceCategory(vm.itemType() == CoverageRuleTarget.CATEGORY ? requireCategory(vm.serviceCategory()) : null)
                .serviceId(vm.itemType() == CoverageRuleTarget.SERVICE ? requireService(vm.serviceId()).getId() : null)
                .allDiagnoses(vm.itemType() == CoverageRuleTarget.DIAGNOSIS && (Boolean.TRUE.equals(vm.allDiagnoses()) || vm.diagnosisId() == null)
                        || vm.itemType() == CoverageRuleTarget.ALL)
                .diagnosisId(resolveDiagnosisId(vm.itemType() == CoverageRuleTarget.DIAGNOSIS, vm.allDiagnoses(), vm.diagnosisId(), vm.diagnosisCode()))
                .isActive(true)
                .build();
        return toPreApprovalItemVm(preApprovalItemRepository.save(entity));
    }

    public CoveragePreApprovalItemVM deactivatePreApprovalItem(Long id) {
        CoveragePreApprovalItem entity = preApprovalItemRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Pre-approval item was not found.", ENTITY, "notFound"));
        deactivateOnly(entity.getIsActive());
        entity.setIsActive(false);
        return toPreApprovalItemVm(preApprovalItemRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<CoveragePreApprovalItemVM> listPreApprovalItems(Long preApprovalId, Boolean isActive, Pageable pageable) {
        requirePreApproval(preApprovalId);
        Page<CoveragePreApprovalItem> page = isActive == null
                ? preApprovalItemRepository.findByPreApproval_Id(preApprovalId, pageable)
                : preApprovalItemRepository.findByPreApproval_IdAndIsActive(preApprovalId, isActive, pageable);
        return page.map(this::toPreApprovalItemVm);
    }

    private void applyTerm(CoverageTerm entity, CoverageTermVM vm) {
        Facility facility = requireFacility(vm.facilityId());
        boolean allDepartments = Boolean.TRUE.equals(vm.allDepartments()) || vm.departmentId() == null;
        Department department = allDepartments ? null : requireDepartment(vm.departmentId(), facility.getId());
        Long diagnosisId = resolveDiagnosisId(vm.diagnosisScope() == CoverageDiagnosisScope.SPECIFIC_DIAGNOSIS, false, vm.diagnosisId(), vm.diagnosisCode());
        entity.setTermType(vm.termType());
        entity.setDiagnosisScope(vm.diagnosisScope());
        entity.setDiagnosisId(diagnosisId);
        entity.setFacilityId(facility.getId());
        entity.setAllDepartments(allDepartments);
        entity.setDepartmentId(department == null ? null : department.getId());
        entity.setEncounterType(resolveEncounterType(department, vm.encounterType()));
        entity.setPeriodBasis(vm.termType() == CoverageTermType.COVERAGE ? null : vm.periodBasis());
        entity.setCoverageBasis(vm.termType() == CoverageTermType.COVERAGE ? null : vm.coverageBasis());
        entity.setValueType(vm.valueType());
        entity.setLimitValue(vm.limitValue());
        if (vm.termType() != CoverageTermType.COVERAGE && vm.periodBasis() == null) {
            throw new BadRequestAlertException("Period type is required.", ENTITY, "periodBasisRequired");
        }
        if (vm.termType() != CoverageTermType.COVERAGE && vm.coverageBasis() == null) {
            throw new BadRequestAlertException("Gross/Net basis is required.", ENTITY, "coverageBasisRequired");
        }
        requirePositive(vm.limitValue(), "Limit value");
    }

    private void applyTermItem(CoverageTermItem entity, CoverageTermItemVM vm) {
        entity.setCategoryScope(vm.categoryScope());
        entity.setServiceCategory(vm.categoryScope() == CoverageRuleTarget.CATEGORY ? requireCategory(vm.serviceCategory()) : null);
        entity.setServiceId(vm.serviceId() == null ? null : requireService(vm.serviceId()).getId());
        entity.setValueType(vm.valueType());
        entity.setLimitValue(vm.limitValue());
        requirePositive(vm.limitValue(), "Limit value");
    }

    private void validateTerm(CoverageTermVM vm) {
        if (vm.termType() == null) {
            throw new BadRequestAlertException("Term type is required.", ENTITY, "termTypeRequired");
        }
        if (vm.diagnosisScope() == CoverageDiagnosisScope.SPECIFIC_DIAGNOSIS && vm.diagnosisId() == null) {
            throw new BadRequestAlertException("Specific diagnosis is required.", ENTITY, "diagnosisRequired");
        }
    }

    private void validateTermItem(CoverageTermItemVM vm) {
        if (vm.categoryScope() == CoverageRuleTarget.CATEGORY && vm.serviceCategory() == null) {
            throw new BadRequestAlertException("Category is required.", ENTITY, "categoryRequired");
        }
        if (vm.categoryScope() != CoverageRuleTarget.ALL
                && vm.categoryScope() != CoverageRuleTarget.CATEGORY) {
            throw new BadRequestAlertException("Category must be All or a specific category.", ENTITY, "invalidCategoryScope");
        }
    }

    private void validateDiscount(CoverageDiscountVM vm) {
        if (vm.targetType() == CoverageRuleTarget.DIAGNOSIS) {
            throw new BadRequestAlertException("Discount can apply to all items, a category, or a service.", ENTITY, "invalidTarget");
        }
        if (vm.targetType() == CoverageRuleTarget.CATEGORY) {
            requireCategory(vm.serviceCategory());
        }
        if (vm.targetType() == CoverageRuleTarget.SERVICE) {
            requireService(vm.serviceId());
        }
        requirePositive(vm.discountValue(), "Discount value");
    }

    private void validateExclusion(CoverageExclusionVM vm) {
        if (vm.exclusionType() == CoverageRuleTarget.ALL) {
            throw new BadRequestAlertException("Exclusion type must be category, service, or diagnosis.", ENTITY, "invalidExclusion");
        }
        if (vm.exclusionType() == CoverageRuleTarget.CATEGORY) {
            requireCategory(vm.serviceCategory());
        }
        if (vm.exclusionType() == CoverageRuleTarget.SERVICE) {
            requireService(vm.serviceId());
        }
        if (vm.excludedResult() != YesNoQuestion.YES && vm.excludedResult() != YesNoQuestion.NO) {
            throw new BadRequestAlertException("Excluded result must be Yes or No.", ENTITY, "invalidExcludedResult");
        }
    }

    private void validatePreApproval(CoveragePreApprovalVM vm) {
        if (vm.approvalScope() == CoverageApprovalScope.FACILITY) {
            requireFacility(vm.facilityId());
            requireEncounterType(vm.encounterType());
        } else if (vm.approvalScope() == CoverageApprovalScope.DEPARTMENT) {
            requireDepartment(vm.departmentId(), null);
        }
    }

    private void validatePreApprovalItem(CoveragePreApprovalItemVM vm) {
        if (vm.itemType() == CoverageRuleTarget.CATEGORY) {
            requireCategory(vm.serviceCategory());
        }
        if (vm.itemType() == CoverageRuleTarget.SERVICE) {
            requireService(vm.serviceId());
        }
    }

    private EncounterType resolveEncounterType(Department department, EncounterType requested) {
        if (requested != null) {
            return requested;
        }
        return department != null ? department.getEncounterType() : EncounterType.ALL;
    }

    private Long resolveServiceId(CoverageRuleTarget targetType, Long serviceId) {
        if (targetType == CoverageRuleTarget.SERVICE) {
            return requireService(serviceId).getId();
        }
        return null;
    }

    private Long resolveDiagnosisId(boolean specific, Boolean allDiagnoses, Long diagnosisId, String diagnosisCode) {
        if (!specific || Boolean.TRUE.equals(allDiagnoses)) {
            return null;
        }
        if (diagnosisId == null && (diagnosisCode == null || diagnosisCode.isBlank())) {
            return null;
        }
        return coverageLookupService.requireDiagnosis(diagnosisId, diagnosisCode).getId();
    }

    private void deactivateOnly(Boolean currentlyActive) {
        if (!Boolean.TRUE.equals(currentlyActive)) {
            throw new BadRequestAlertException("Only an active record can be changed to inactive.", ENTITY, "alreadyInactive");
        }
    }

    private CoverageCopayment requireOwnedCopayment(Long id, Long contractId) {
        CoverageCopayment entity = copaymentRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Co-payment was not found.", ENTITY, "notFound"));
        if (!entity.getCoverageContract().getId().equals(contractId)) {
            throw new BadRequestAlertException("Co-payment does not belong to this contract.", ENTITY, "contractMismatch");
        }
        return entity;
    }

    private CoverageTerm requireOwnedTerm(Long id, Long contractId) {
        CoverageTerm entity = requireTerm(id);
        if (!entity.getCoverageContract().getId().equals(contractId)) {
            throw new BadRequestAlertException("Coverage term does not belong to this contract.", ENTITY, "contractMismatch");
        }
        return entity;
    }

    private CoverageTerm requireTerm(Long id) {
        return termRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Coverage term was not found.", ENTITY, "notFound"));
    }

    private CoverageTermItem requireOwnedTermItem(Long id, Long termId) {
        CoverageTermItem entity = termItemRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Coverage term item was not found.", ENTITY, "notFound"));
        if (!entity.getCoverageTerm().getId().equals(termId)) {
            throw new BadRequestAlertException("Item does not belong to this coverage term.", ENTITY, "termMismatch");
        }
        return entity;
    }

    private CoveragePreApproval requireOwnedPreApproval(Long id, Long contractId) {
        CoveragePreApproval entity = requirePreApproval(id);
        if (!entity.getCoverageContract().getId().equals(contractId)) {
            throw new BadRequestAlertException("Pre-approval does not belong to this contract.", ENTITY, "contractMismatch");
        }
        return entity;
    }

    private CoveragePreApproval requirePreApproval(Long id) {
        return preApprovalRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Pre-approval was not found.", ENTITY, "notFound"));
    }

    private Facility requireFacility(Long id) {
        if (id == null) {
            throw new BadRequestAlertException("Facility is required.", ENTITY, "facilityRequired");
        }
        return coverageLookupService.requireActiveFacility(id);
    }

    private Department requireDepartment(Long id, Long facilityId) {
        if (id == null) {
            throw new BadRequestAlertException("Department is required.", ENTITY, "departmentRequired");
        }
        return coverageLookupService.requireActiveDepartment(id, facilityId);
    }

    private ServiceSetup requireService(Long id) {
        if (id == null) {
            throw new BadRequestAlertException("Service is required.", ENTITY, "serviceRequired");
        }
        return coverageLookupService.requireActiveService(id);
    }

    private ServiceCategory requireCategory(ServiceCategory category) {
        if (category == null) {
            throw new BadRequestAlertException("Category is required.", ENTITY, "categoryRequired");
        }
        return category;
    }

    private EncounterType requireEncounterType(EncounterType encounterType) {
        if (encounterType == null) {
            throw new BadRequestAlertException("Encounter type is required.", ENTITY, "encounterTypeRequired");
        }
        return encounterType;
    }

    private void requirePositive(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestAlertException(field + " must be greater than zero.", ENTITY, "invalidValue");
        }
    }

    private CoverageTermVM toTermVm(CoverageTerm entity) {
        Facility facility = coverageLookupService.findFacility(entity.getFacilityId());
        Department department = coverageLookupService.findDepartment(entity.getDepartmentId());
        ICDDiagnosis diagnosis = coverageLookupService.findDiagnosis(entity.getDiagnosisId());
        return new CoverageTermVM(
                entity.getId(),
                entity.getTermType(),
                entity.getDiagnosisScope(),
                entity.getDiagnosisId(),
                coverageLookupService.diagnosisCode(diagnosis),
                coverageLookupService.diagnosisName(diagnosis),
                entity.getFacilityId(),
                facility == null ? null : facility.getName(),
                entity.getAllDepartments(),
                entity.getDepartmentId(),
                department == null ? (Boolean.TRUE.equals(entity.getAllDepartments()) ? "All" : null) : department.getName(),
                entity.getEncounterType(),
                entity.getPeriodBasis(),
                entity.getCoverageBasis(),
                entity.getValueType(),
                entity.getLimitValue(),
                entity.getIsActive(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate()
        );
    }

    private CoverageTermItemVM toTermItemVm(CoverageTermItem entity) {
        ServiceSetup service = coverageLookupService.findService(entity.getServiceId());
        return new CoverageTermItemVM(
                entity.getId(),
                entity.getCategoryScope(),
                entity.getServiceCategory(),
                entity.getServiceId(),
                service == null ? null : service.getCode(),
                service == null ? (entity.getCategoryScope() == CoverageRuleTarget.ALL ? "All" : null) : service.getName(),
                entity.getValueType(),
                entity.getLimitValue(),
                entity.getIsActive(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate()
        );
    }

    private CoverageDiscountVM toDiscountVm(CoverageDiscount entity) {
        ServiceSetup service = coverageLookupService.findService(entity.getServiceId());
        return new CoverageDiscountVM(
                entity.getId(),
                entity.getTargetType(),
                entity.getServiceCategory(),
                entity.getServiceId(),
                service == null ? null : service.getCode(),
                service == null ? (entity.getTargetType() == CoverageRuleTarget.ALL ? "All items" : null) : service.getName(),
                entity.getEncounterType(),
                entity.getDiscountType(),
                entity.getDiscountValue(),
                entity.getIsActive(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate()
        );
    }

    private CoverageExclusionVM toExclusionVm(CoverageExclusion entity) {
        ServiceSetup service = coverageLookupService.findService(entity.getServiceId());
        ICDDiagnosis diagnosis = coverageLookupService.findDiagnosis(entity.getDiagnosisId());
        return new CoverageExclusionVM(
                entity.getId(),
                entity.getExclusionType(),
                entity.getServiceCategory(),
                entity.getServiceId(),
                service == null ? null : service.getCode(),
                service == null ? null : service.getName(),
                entity.getAllDiagnoses(),
                entity.getDiagnosisId(),
                diagnosis == null ? (Boolean.TRUE.equals(entity.getAllDiagnoses()) ? "ALL" : null) : coverageLookupService.diagnosisCode(diagnosis),
                diagnosis == null ? (Boolean.TRUE.equals(entity.getAllDiagnoses()) ? "All diagnoses" : null) : coverageLookupService.diagnosisName(diagnosis),
                entity.getEncounterType(),
                entity.getExcludedResult(),
                entity.getIsActive(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate()
        );
    }

    private CoveragePreApprovalVM toPreApprovalVm(CoveragePreApproval entity) {
        Facility facility = coverageLookupService.findFacility(entity.getFacilityId());
        Department department = coverageLookupService.findDepartment(entity.getDepartmentId());
        return new CoveragePreApprovalVM(
                entity.getId(),
                entity.getApprovalScope(),
                entity.getFacilityId(),
                facility == null ? null : facility.getName(),
                entity.getDepartmentId(),
                department == null ? null : department.getName(),
                entity.getEncounterType(),
                entity.getIsActive(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate()
        );
    }

    private CoveragePreApprovalItemVM toPreApprovalItemVm(CoveragePreApprovalItem entity) {
        ServiceSetup service = coverageLookupService.findService(entity.getServiceId());
        ICDDiagnosis diagnosis = coverageLookupService.findDiagnosis(entity.getDiagnosisId());
        return new CoveragePreApprovalItemVM(
                entity.getId(),
                entity.getItemType(),
                entity.getServiceCategory(),
                entity.getServiceId(),
                service == null ? null : service.getCode(),
                service == null ? (entity.getItemType() == CoverageRuleTarget.ALL ? "All" : null) : service.getName(),
                entity.getAllDiagnoses(),
                entity.getDiagnosisId(),
                diagnosis == null ? (Boolean.TRUE.equals(entity.getAllDiagnoses()) ? "ALL" : null) : coverageLookupService.diagnosisCode(diagnosis),
                diagnosis == null ? (entity.getItemType() == CoverageRuleTarget.ALL || Boolean.TRUE.equals(entity.getAllDiagnoses()) ? "All" : null) : coverageLookupService.diagnosisName(diagnosis),
                entity.getIsActive(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate()
        );
    }
}
