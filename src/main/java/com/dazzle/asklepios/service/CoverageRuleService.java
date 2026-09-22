package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageClass;
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
import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.domain.enumeration.CoverageApprovalScope;
import com.dazzle.asklepios.domain.enumeration.CoverageDiagnosisScope;
import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.CoverageTermType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.patient.YesNoQuestion;
import com.dazzle.asklepios.repository.CoverageCopaymentRepository;
import com.dazzle.asklepios.repository.CoverageDiscountRepository;
import com.dazzle.asklepios.repository.CoverageExclusionRepository;
import com.dazzle.asklepios.repository.CoveragePreApprovalItemRepository;
import com.dazzle.asklepios.repository.CoveragePreApprovalRepository;
import com.dazzle.asklepios.repository.CoverageTermItemRepository;
import com.dazzle.asklepios.repository.CoverageTermRepository;
import com.dazzle.asklepios.repository.TpaDefinitionRepository;
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

    private static final String TPA_ENTITY = "tpaDefinition";

    private final CoverageClassService coverageClassService;
    private final CoverageLookupService coverageLookupService;
    private final TpaDefinitionRepository tpaDefinitionRepository;
    private final CoverageCopaymentRepository copaymentRepository;
    private final CoverageTermRepository termRepository;
    private final CoverageTermItemRepository termItemRepository;
    private final CoverageDiscountRepository discountRepository;
    private final CoverageExclusionRepository exclusionRepository;
    private final CoveragePreApprovalRepository preApprovalRepository;
    private final CoveragePreApprovalItemRepository preApprovalItemRepository;

    public CoverageRuleService(
            CoverageClassService coverageClassService,
            CoverageLookupService coverageLookupService,
            TpaDefinitionRepository tpaDefinitionRepository,
            CoverageCopaymentRepository copaymentRepository,
            CoverageTermRepository termRepository,
            CoverageTermItemRepository termItemRepository,
            CoverageDiscountRepository discountRepository,
            CoverageExclusionRepository exclusionRepository,
            CoveragePreApprovalRepository preApprovalRepository,
            CoveragePreApprovalItemRepository preApprovalItemRepository
    ) {
        this.coverageClassService = coverageClassService;
        this.coverageLookupService = coverageLookupService;
        this.tpaDefinitionRepository = tpaDefinitionRepository;
        this.copaymentRepository = copaymentRepository;
        this.termRepository = termRepository;
        this.termItemRepository = termItemRepository;
        this.discountRepository = discountRepository;
        this.exclusionRepository = exclusionRepository;
        this.preApprovalRepository = preApprovalRepository;
        this.preApprovalItemRepository = preApprovalItemRepository;
    }

    public CoverageCopaymentVM saveCopayment(Long classId, CoverageCopaymentVM vm) {
        CoverageClass coverageClass = coverageClassService.getEntity(classId);
        requirePositive(vm.valueAmount(), "Co-payment value");
        CoverageCopayment entity = vm.id() == null
                ? CoverageCopayment.builder().coverageClass(coverageClass).isActive(true).build()
                : requireOwnedCopayment(vm.id(), classId);
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
    public Page<CoverageCopaymentVM> listCopayments(Long classId, Boolean isActive, Pageable pageable) {
        coverageClassService.getEntity(classId);
        Page<CoverageCopayment> page = isActive == null
                ? copaymentRepository.findByCoverageClass_Id(classId, pageable)
                : copaymentRepository.findByCoverageClass_IdAndIsActive(classId, isActive, pageable);
        return page.map(CoverageCopaymentVM::ofEntity);
    }

    public CoverageTermVM saveTerm(Long classId, CoverageTermVM vm) {
        CoverageClass coverageClass = coverageClassService.getEntity(classId);
        validateTerm(vm);
        CoverageTerm entity = vm.id() == null
                ? CoverageTerm.builder().coverageClass(coverageClass).isActive(true).build()
                : requireOwnedTerm(vm.id(), classId);
        applyTerm(entity, vm);
        if (vm.id() == null) {
            entity.setIsActive(true);
        } else if (vm.isActive() != null) {
            entity.setIsActive(vm.isActive());
        }
        return toTermVm(termRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<CoverageTermVM> listTerms(Long classId, CoverageTermType termType, Boolean isActive, Pageable pageable) {
        coverageClassService.getEntity(classId);
        if (termType == null) {
            throw new BadRequestAlertException("Term type is required.", ENTITY, "termTypeRequired");
        }
        Page<CoverageTerm> page = isActive == null
                ? termRepository.findByCoverageClass_IdAndTermType(classId, termType, pageable)
                : termRepository.findByCoverageClass_IdAndTermTypeAndIsActive(classId, termType, isActive, pageable);
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
        if (vm.isActive() != null) {
            entity.setIsActive(vm.isActive());
        } else if (vm.id() == null) {
            entity.setIsActive(true);
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

    public CoverageDiscountVM createDiscount(Long classId, CoverageDiscountVM vm) {
        CoverageClass coverageClass = coverageClassService.getEntity(classId);
        validateDiscount(vm);
        CoverageDiscount entity = CoverageDiscount.builder()
                .coverageClass(coverageClass)
                .targetType(resolveDiscountTarget(vm))
                .billingItemType(vm.billingItemType())
                .serviceId(resolveDiscountItemId(vm))
                .itemName(blankToNull(vm.serviceName()))
                .encounterType(vm.encounterType())
                .discountType(vm.discountType())
                .discountValue(vm.discountValue())
                .isActive(true)
                .build();
        return toDiscountVm(discountRepository.save(entity));
    }

    public CoverageDiscountVM createTpaDiscount(Long tpaId, CoverageDiscountVM vm) {
        TpaDefinition tpa = requireTpa(tpaId);
        validateDiscount(vm);
        CoverageDiscount entity = CoverageDiscount.builder()
                .tpaDefinition(tpa)
                .targetType(resolveDiscountTarget(vm))
                .billingItemType(vm.billingItemType())
                .serviceId(resolveDiscountItemId(vm))
                .itemName(blankToNull(vm.serviceName()))
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
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        return toDiscountVm(discountRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<CoverageDiscountVM> listDiscounts(Long classId, Boolean isActive, Pageable pageable) {
        coverageClassService.getEntity(classId);
        Page<CoverageDiscount> page = isActive == null
                ? discountRepository.findByCoverageClass_Id(classId, pageable)
                : discountRepository.findByCoverageClass_IdAndIsActive(classId, isActive, pageable);
        return page.map(this::toDiscountVm);
    }

    @Transactional(readOnly = true)
    public Page<CoverageDiscountVM> listTpaDiscounts(Long tpaId, Boolean isActive, Pageable pageable) {
        requireTpa(tpaId);
        Page<CoverageDiscount> page = isActive == null
                ? discountRepository.findByTpaDefinition_Id(tpaId, pageable)
                : discountRepository.findByTpaDefinition_IdAndIsActive(tpaId, isActive, pageable);
        return page.map(this::toDiscountVm);
    }

    public CoverageExclusionVM createExclusion(Long classId, CoverageExclusionVM vm) {
        CoverageClass coverageClass = coverageClassService.getEntity(classId);
        validateExclusion(vm);
        CoverageRuleTarget type = resolveExclusionType(vm);
        CoverageExclusion entity = CoverageExclusion.builder()
                .coverageClass(coverageClass)
                .exclusionType(type)
                .billingItemType(type == CoverageRuleTarget.DIAGNOSIS ? null : vm.billingItemType())
                .serviceId(resolveExclusionItemId(vm))
                .itemName(type == CoverageRuleTarget.DIAGNOSIS ? null : blankToNull(vm.serviceName()))
                .allDiagnoses(Boolean.TRUE.equals(vm.allDiagnoses()) || type == CoverageRuleTarget.DIAGNOSIS && vm.diagnosisId() == null)
                .diagnosisId(resolveDiagnosisId(type == CoverageRuleTarget.DIAGNOSIS, vm.allDiagnoses(), vm.diagnosisId(), vm.diagnosisCode()))
                .encounterType(vm.encounterType())
                .excludedResult(vm.excludedResult() != null ? vm.excludedResult() : YesNoQuestion.YES)
                .isActive(true)
                .build();
        return toExclusionVm(exclusionRepository.save(entity));
    }

    public CoverageExclusionVM createTpaExclusion(Long tpaId, CoverageExclusionVM vm) {
        TpaDefinition tpa = requireTpa(tpaId);
        validateExclusion(vm);
        CoverageRuleTarget type = resolveExclusionType(vm);
        CoverageExclusion entity = CoverageExclusion.builder()
                .tpaDefinition(tpa)
                .exclusionType(type)
                .billingItemType(type == CoverageRuleTarget.DIAGNOSIS ? null : vm.billingItemType())
                .serviceId(resolveExclusionItemId(vm))
                .itemName(type == CoverageRuleTarget.DIAGNOSIS ? null : blankToNull(vm.serviceName()))
                .allDiagnoses(Boolean.TRUE.equals(vm.allDiagnoses()) || type == CoverageRuleTarget.DIAGNOSIS && vm.diagnosisId() == null)
                .diagnosisId(resolveDiagnosisId(type == CoverageRuleTarget.DIAGNOSIS, vm.allDiagnoses(), vm.diagnosisId(), vm.diagnosisCode()))
                .encounterType(vm.encounterType())
                .excludedResult(vm.excludedResult() != null ? vm.excludedResult() : YesNoQuestion.YES)
                .isActive(true)
                .build();
        return toExclusionVm(exclusionRepository.save(entity));
    }

    public CoverageExclusionVM deactivateExclusion(Long id) {
        CoverageExclusion entity = exclusionRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Exclusion was not found.", ENTITY, "notFound"));
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        return toExclusionVm(exclusionRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<CoverageExclusionVM> listExclusions(Long classId, Boolean isActive, Pageable pageable) {
        coverageClassService.getEntity(classId);
        Page<CoverageExclusion> page = isActive == null
                ? exclusionRepository.findByCoverageClass_Id(classId, pageable)
                : exclusionRepository.findByCoverageClass_IdAndIsActive(classId, isActive, pageable);
        return page.map(this::toExclusionVm);
    }

    @Transactional(readOnly = true)
    public Page<CoverageExclusionVM> listTpaExclusions(Long tpaId, Boolean isActive, Pageable pageable) {
        requireTpa(tpaId);
        Page<CoverageExclusion> page = isActive == null
                ? exclusionRepository.findByTpaDefinition_Id(tpaId, pageable)
                : exclusionRepository.findByTpaDefinition_IdAndIsActive(tpaId, isActive, pageable);
        return page.map(this::toExclusionVm);
    }

    public CoveragePreApprovalVM savePreApproval(Long classId, CoveragePreApprovalVM vm) {
        CoverageClass coverageClass = coverageClassService.getEntity(classId);
        validatePreApproval(vm);
        CoveragePreApproval entity = vm.id() == null
                ? CoveragePreApproval.builder().coverageClass(coverageClass).isActive(true).build()
                : requireOwnedPreApproval(vm.id(), classId);
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

    public CoveragePreApprovalVM saveTpaPreApproval(Long tpaId, CoveragePreApprovalVM vm) {
        TpaDefinition tpa = requireTpa(tpaId);
        validatePreApproval(vm);
        CoveragePreApproval entity = vm.id() == null
                ? CoveragePreApproval.builder().tpaDefinition(tpa).isActive(true).build()
                : requireOwnedTpaPreApproval(vm.id(), tpaId);
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
    public Page<CoveragePreApprovalVM> listPreApprovals(Long classId, Boolean isActive, Pageable pageable) {
        coverageClassService.getEntity(classId);
        Page<CoveragePreApproval> page = isActive == null
                ? preApprovalRepository.findByCoverageClass_Id(classId, pageable)
                : preApprovalRepository.findByCoverageClass_IdAndIsActive(classId, isActive, pageable);
        return page.map(this::toPreApprovalVm);
    }

    @Transactional(readOnly = true)
    public Page<CoveragePreApprovalVM> listTpaPreApprovals(Long tpaId, Boolean isActive, Pageable pageable) {
        requireTpa(tpaId);
        Page<CoveragePreApproval> page = isActive == null
                ? preApprovalRepository.findByTpaDefinition_Id(tpaId, pageable)
                : preApprovalRepository.findByTpaDefinition_IdAndIsActive(tpaId, isActive, pageable);
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
        CoverageRuleTarget scope = resolveTermItemScope(vm);
        entity.setCategoryScope(scope);
        entity.setBillingItemType(vm.billingItemType());
        entity.setServiceId(resolveTermItemId(vm));
        entity.setItemName(blankToNull(vm.serviceName()));
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
        if (vm.billingItemType() == null && vm.categoryScope() != null
                && vm.categoryScope() != CoverageRuleTarget.ALL) {
            throw new BadRequestAlertException("Category is required.", ENTITY, "categoryRequired");
        }
        if (vm.serviceId() != null && vm.billingItemType() == BillingItemTypes.SERVICE) {
            requireService(vm.serviceId());
        }
        requirePositive(vm.limitValue(), "Limit value");
    }

    private CoverageRuleTarget resolveTermItemScope(CoverageTermItemVM vm) {
        if (vm.billingItemType() != null) {
            return vm.serviceId() != null ? CoverageRuleTarget.SERVICE : CoverageRuleTarget.CATEGORY;
        }
        return CoverageRuleTarget.ALL;
    }

    private Long resolveTermItemId(CoverageTermItemVM vm) {
        CoverageRuleTarget scope = resolveTermItemScope(vm);
        if (scope != CoverageRuleTarget.SERVICE || vm.serviceId() == null) {
            return null;
        }
        if (vm.billingItemType() == null || vm.billingItemType() == BillingItemTypes.SERVICE) {
            return requireService(vm.serviceId()).getId();
        }
        return vm.serviceId();
    }

    private void validateDiscount(CoverageDiscountVM vm) {
        requirePositive(vm.discountValue(), "Discount value");
        if (vm.targetType() == CoverageRuleTarget.DIAGNOSIS) {
            throw new BadRequestAlertException("Discount can apply to all items, a category, or a service.", ENTITY, "invalidTarget");
        }
        if (vm.serviceId() != null && vm.billingItemType() == BillingItemTypes.SERVICE) {
            requireService(vm.serviceId());
        }
    }

    private void validateExclusion(CoverageExclusionVM vm) {
        CoverageRuleTarget type = resolveExclusionType(vm);
        if (type == CoverageRuleTarget.DIAGNOSIS
                && !Boolean.TRUE.equals(vm.allDiagnoses())
                && vm.diagnosisId() == null
                && (vm.diagnosisCode() == null || vm.diagnosisCode().isBlank())) {
            throw new BadRequestAlertException("Diagnosis is required.", ENTITY, "diagnosisRequired");
        }
        if (vm.serviceId() != null && vm.billingItemType() == BillingItemTypes.SERVICE) {
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

    private CoverageRuleTarget resolveDiscountTarget(CoverageDiscountVM vm) {
        if (vm.billingItemType() != null) {
            return vm.serviceId() != null ? CoverageRuleTarget.SERVICE : CoverageRuleTarget.CATEGORY;
        }
        return vm.targetType() != null ? vm.targetType() : CoverageRuleTarget.ALL;
    }

    private Long resolveDiscountItemId(CoverageDiscountVM vm) {
        CoverageRuleTarget target = resolveDiscountTarget(vm);
        if (target != CoverageRuleTarget.SERVICE || vm.serviceId() == null) {
            return null;
        }
        if (vm.billingItemType() == null || vm.billingItemType() == BillingItemTypes.SERVICE) {
            return requireService(vm.serviceId()).getId();
        }
        return vm.serviceId();
    }

    private CoverageRuleTarget resolveExclusionType(CoverageExclusionVM vm) {
        if (vm.exclusionType() == CoverageRuleTarget.DIAGNOSIS) {
            return CoverageRuleTarget.DIAGNOSIS;
        }
        if (vm.billingItemType() != null) {
            return vm.serviceId() != null ? CoverageRuleTarget.SERVICE : CoverageRuleTarget.CATEGORY;
        }
        return CoverageRuleTarget.ALL;
    }

    private Long resolveExclusionItemId(CoverageExclusionVM vm) {
        CoverageRuleTarget type = resolveExclusionType(vm);
        if (type != CoverageRuleTarget.SERVICE || vm.serviceId() == null) {
            return null;
        }
        if (vm.billingItemType() == null || vm.billingItemType() == BillingItemTypes.SERVICE) {
            return requireService(vm.serviceId()).getId();
        }
        return vm.serviceId();
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

    private CoverageCopayment requireOwnedCopayment(Long id, Long classId) {
        CoverageCopayment entity = copaymentRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Co-payment was not found.", ENTITY, "notFound"));
        if (entity.getCoverageClass() == null || !entity.getCoverageClass().getId().equals(classId)) {
            throw new BadRequestAlertException("Co-payment does not belong to this class.", ENTITY, "classMismatch");
        }
        return entity;
    }

    private CoverageTerm requireOwnedTerm(Long id, Long classId) {
        CoverageTerm entity = requireTerm(id);
        if (entity.getCoverageClass() == null || !entity.getCoverageClass().getId().equals(classId)) {
            throw new BadRequestAlertException("Coverage term does not belong to this class.", ENTITY, "classMismatch");
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

    private CoveragePreApproval requireOwnedPreApproval(Long id, Long classId) {
        CoveragePreApproval entity = requirePreApproval(id);
        if (entity.getCoverageClass() == null || !entity.getCoverageClass().getId().equals(classId)) {
            throw new BadRequestAlertException("Pre-approval does not belong to this class.", ENTITY, "classMismatch");
        }
        return entity;
    }

    private CoveragePreApproval requireOwnedTpaPreApproval(Long id, Long tpaId) {
        CoveragePreApproval entity = requirePreApproval(id);
        if (entity.getTpaDefinition() == null || !entity.getTpaDefinition().getId().equals(tpaId)) {
            throw new BadRequestAlertException("Pre-approval does not belong to this TPA.", TPA_ENTITY, "tpaMismatch");
        }
        return entity;
    }

    private TpaDefinition requireTpa(Long tpaId) {
        return tpaDefinitionRepository.findById(tpaId)
                .orElseThrow(() -> new BadRequestAlertException("TPA was not found.", TPA_ENTITY, "notFound"));
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
        ServiceSetup service =
                entity.getBillingItemType() == null || entity.getBillingItemType() == BillingItemTypes.SERVICE
                        ? coverageLookupService.findService(entity.getServiceId())
                        : null;
        String itemName = blankToNull(entity.getItemName());
        if (itemName == null && service != null) {
            itemName = service.getName();
        }
        if (itemName == null && entity.getCategoryScope() == CoverageRuleTarget.ALL) {
            itemName = "All";
        }
        return new CoverageTermItemVM(
                entity.getId(),
                entity.getCategoryScope(),
                entity.getBillingItemType(),
                entity.getServiceId(),
                service == null ? null : service.getCode(),
                itemName,
                entity.getValueType(),
                entity.getLimitValue(),
                entity.getIsActive(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate()
        );
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private CoverageDiscountVM toDiscountVm(CoverageDiscount entity) {
        ServiceSetup service =
                entity.getBillingItemType() == null || entity.getBillingItemType() == BillingItemTypes.SERVICE
                        ? coverageLookupService.findService(entity.getServiceId())
                        : null;
        String itemName = blankToNull(entity.getItemName());
        if (itemName == null && service != null) {
            itemName = service.getName();
        }
        if (itemName == null && entity.getTargetType() == CoverageRuleTarget.ALL) {
            itemName = "All";
        }
        return new CoverageDiscountVM(
                entity.getId(),
                entity.getTargetType(),
                entity.getBillingItemType(),
                entity.getServiceId(),
                service == null ? null : service.getCode(),
                itemName,
                entity.getEncounterType(),
                entity.getDiscountType(),
                entity.getDiscountValue(),
                entity.getIsActive(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate()
        );
    }

    private CoverageExclusionVM toExclusionVm(CoverageExclusion entity) {
        ServiceSetup service =
                entity.getBillingItemType() == null || entity.getBillingItemType() == BillingItemTypes.SERVICE
                        ? coverageLookupService.findService(entity.getServiceId())
                        : null;
        ICDDiagnosis diagnosis = coverageLookupService.findDiagnosis(entity.getDiagnosisId());
        String itemName = blankToNull(entity.getItemName());
        if (itemName == null && service != null) {
            itemName = service.getName();
        }
        if (itemName == null && entity.getExclusionType() == CoverageRuleTarget.ALL) {
            itemName = "All";
        }
        return new CoverageExclusionVM(
                entity.getId(),
                entity.getExclusionType(),
                entity.getBillingItemType(),
                entity.getServiceId(),
                service == null ? null : service.getCode(),
                itemName,
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
