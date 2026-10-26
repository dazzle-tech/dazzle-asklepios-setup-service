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
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;
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
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageDiagnosisRefVM;
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
import java.util.ArrayList;
import java.util.List;

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

    public CoverageCopaymentVM toggleCopaymentActive(Long id) {
        CoverageCopayment entity = copaymentRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Co-payment was not found.", ENTITY, "notFound"));
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        return CoverageCopaymentVM.ofEntity(copaymentRepository.save(entity));
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

    public CoverageTermVM toggleTermActive(Long id) {
        CoverageTerm entity = requireTerm(id);
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        return toTermVm(termRepository.save(entity));
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
        CoverageDiscount entity = CoverageDiscount.builder()
                .coverageClass(coverageClass)
                .isActive(true)
                .build();
        applyDiscount(entity, vm);
        return toDiscountVm(discountRepository.save(entity));
    }

    public CoverageDiscountVM updateDiscount(Long classId, CoverageDiscountVM vm) {
        coverageClassService.getEntity(classId);
        CoverageDiscount entity = requireOwnedDiscount(vm.id(), classId);
        applyDiscount(entity, vm);
        return toDiscountVm(discountRepository.save(entity));
    }

    public CoverageDiscountVM createTpaDiscount(Long tpaId, CoverageDiscountVM vm) {
        TpaDefinition tpa = requireTpa(tpaId);
        CoverageDiscount entity = CoverageDiscount.builder()
                .tpaDefinition(tpa)
                .isActive(true)
                .build();
        applyDiscount(entity, vm);
        return toDiscountVm(discountRepository.save(entity));
    }

    public CoverageDiscountVM updateTpaDiscount(Long tpaId, CoverageDiscountVM vm) {
        requireTpa(tpaId);
        CoverageDiscount entity = requireOwnedTpaDiscount(vm.id(), tpaId);
        applyDiscount(entity, vm);
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
        CoverageExclusion entity = CoverageExclusion.builder()
                .coverageClass(coverageClass)
                .diagnosisIds(new ArrayList<>())
                .isActive(true)
                .build();
        applyExclusion(entity, vm);
        return toExclusionVm(exclusionRepository.save(entity));
    }

    public CoverageExclusionVM updateExclusion(Long classId, CoverageExclusionVM vm) {
        coverageClassService.getEntity(classId);
        CoverageExclusion entity = requireOwnedExclusion(vm.id(), classId);
        applyExclusion(entity, vm);
        return toExclusionVm(exclusionRepository.save(entity));
    }

    public CoverageExclusionVM createTpaExclusion(Long tpaId, CoverageExclusionVM vm) {
        TpaDefinition tpa = requireTpa(tpaId);
        CoverageExclusion entity = CoverageExclusion.builder()
                .tpaDefinition(tpa)
                .diagnosisIds(new ArrayList<>())
                .isActive(true)
                .build();
        applyExclusion(entity, vm);
        return toExclusionVm(exclusionRepository.save(entity));
    }

    public CoverageExclusionVM updateTpaExclusion(Long tpaId, CoverageExclusionVM vm) {
        requireTpa(tpaId);
        CoverageExclusion entity = requireOwnedTpaExclusion(vm.id(), tpaId);
        applyExclusion(entity, vm);
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
        applyPreApproval(entity, vm);
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
        applyPreApproval(entity, vm);
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

    public CoveragePreApprovalVM togglePreApprovalActive(Long id) {
        CoveragePreApproval entity = requirePreApproval(id);
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        return toPreApprovalVm(preApprovalRepository.save(entity));
    }

    public CoveragePreApprovalItemVM createPreApprovalItem(Long preApprovalId, CoveragePreApprovalItemVM vm) {
        CoveragePreApproval parent = requirePreApproval(preApprovalId);
        if (vm.id() == null && !Boolean.TRUE.equals(parent.getIsActive())) {
            throw new BadRequestAlertException("Pre-approval must be active to add items.", ENTITY, "inactiveParent");
        }
        CoveragePreApprovalItem entity = vm.id() == null
                ? CoveragePreApprovalItem.builder()
                    .preApproval(parent)
                    .diagnosisIds(new ArrayList<>())
                    .isActive(true)
                    .build()
                : requireOwnedPreApprovalItem(vm.id(), preApprovalId);
        applyPreApprovalItem(entity, vm);
        if (vm.id() == null) {
            entity.setIsActive(true);
        }
        return toPreApprovalItemVm(preApprovalItemRepository.save(entity));
    }

    public CoveragePreApprovalItemVM deactivatePreApprovalItem(Long id) {
        CoveragePreApprovalItem entity = preApprovalItemRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Pre-approval item was not found.", ENTITY, "notFound"));
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
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
        List<Long> diagnosisIds = resolveSelectedDiagnosisIds(
                vm.diagnosisScope() == CoverageDiagnosisScope.SPECIFIC_DIAGNOSIS,
                false,
                vm.diagnoses(),
                vm.diagnosisIds(),
                vm.diagnosisId(),
                vm.diagnosisCode()
        );
        entity.setTermType(vm.termType());
        entity.setDiagnosisScope(vm.diagnosisScope());
        entity.setDiagnosisIds(CoverageDiagnosisSelection.ensureMutable(entity.getDiagnosisIds()));
        CoverageDiagnosisSelection.replace(entity.getDiagnosisIds(), diagnosisIds);
        entity.setDiagnosisId(CoverageDiagnosisSelection.first(diagnosisIds));
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
        if (vm.diagnosisScope() == CoverageDiagnosisScope.SPECIFIC_DIAGNOSIS
                && CoverageDiagnosisSelection.isEmpty(requestedDiagnosisIds(vm.diagnoses(), vm.diagnosisIds()), vm.diagnosisId())
                && (vm.diagnosisCode() == null || vm.diagnosisCode().isBlank())) {
            throw new BadRequestAlertException("At least one specific diagnosis is required.", ENTITY, "diagnosisRequired");
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

    private void applyDiscount(CoverageDiscount entity, CoverageDiscountVM vm) {
        validateDiscount(vm);
        entity.setTargetType(resolveDiscountTarget(vm));
        entity.setBillingItemType(vm.billingItemType());
        entity.setServiceId(resolveDiscountItemId(vm));
        entity.setItemName(blankToNull(vm.serviceName()));
        entity.setEncounterType(vm.encounterType());
        entity.setDiscountType(vm.discountType());
        entity.setDiscountValue(vm.discountValue());
    }

    private void applyExclusion(CoverageExclusion entity, CoverageExclusionVM vm) {
        validateExclusion(vm);
        CoverageRuleTarget type = resolveExclusionType(vm);
        List<Long> diagnosisIds = resolveSelectedDiagnosisIds(
                type == CoverageRuleTarget.DIAGNOSIS,
                vm.allDiagnoses(),
                vm.diagnoses(),
                vm.diagnosisIds(),
                vm.diagnosisId(),
                vm.diagnosisCode()
        );
        entity.setExclusionType(type);
        entity.setBillingItemType(type == CoverageRuleTarget.DIAGNOSIS ? null : vm.billingItemType());
        entity.setServiceId(resolveExclusionItemId(vm));
        entity.setItemName(type == CoverageRuleTarget.DIAGNOSIS ? null : blankToNull(vm.serviceName()));
        entity.setAllDiagnoses(allDiagnosesSelected(type == CoverageRuleTarget.DIAGNOSIS, vm.allDiagnoses(), diagnosisIds));
        entity.setDiagnosisIds(CoverageDiagnosisSelection.ensureMutable(entity.getDiagnosisIds()));
        CoverageDiagnosisSelection.replace(entity.getDiagnosisIds(), diagnosisIds);
        entity.setDiagnosisId(CoverageDiagnosisSelection.first(diagnosisIds));
        entity.setEncounterType(vm.encounterType());
        entity.setExcludedResult(vm.excludedResult() != null ? vm.excludedResult() : YesNoQuestion.YES);
    }

    private void applyPreApprovalItem(CoveragePreApprovalItem entity, CoveragePreApprovalItemVM vm) {
        validatePreApprovalItem(vm);
        List<Long> diagnosisIds = resolveSelectedDiagnosisIds(
                vm.itemType() == CoverageRuleTarget.DIAGNOSIS,
                vm.allDiagnoses(),
                vm.diagnoses(),
                vm.diagnosisIds(),
                vm.diagnosisId(),
                vm.diagnosisCode()
        );
        entity.setItemType(vm.itemType());
        if (vm.itemType() == CoverageRuleTarget.CATEGORY) {
            BillingItemTypes category = requireCategory(vm.serviceCategory());
            Long itemId = resolveCategoryItemId(category, vm.serviceId());
            entity.setServiceCategory(category);
            entity.setServiceId(itemId);
            entity.setItemName(itemId == null ? null : blankToNull(vm.serviceName()));
        } else if (vm.itemType() == CoverageRuleTarget.SERVICE) {
            ServiceSetup service = requireService(vm.serviceId());
            entity.setServiceCategory(null);
            entity.setServiceId(service.getId());
            entity.setItemName(firstNonBlank(blankToNull(vm.serviceName()), service.getName()));
        } else {
            entity.setServiceCategory(null);
            entity.setServiceId(null);
            entity.setItemName(null);
        }
        entity.setAllDiagnoses(vm.itemType() == CoverageRuleTarget.ALL
                || allDiagnosesSelected(vm.itemType() == CoverageRuleTarget.DIAGNOSIS, vm.allDiagnoses(), diagnosisIds));
        entity.setDiagnosisIds(CoverageDiagnosisSelection.ensureMutable(entity.getDiagnosisIds()));
        CoverageDiagnosisSelection.replace(entity.getDiagnosisIds(), diagnosisIds);
        entity.setDiagnosisId(CoverageDiagnosisSelection.first(diagnosisIds));
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
                && CoverageDiagnosisSelection.isEmpty(requestedDiagnosisIds(vm.diagnoses(), vm.diagnosisIds()), vm.diagnosisId())
                && (vm.diagnosisCode() == null || vm.diagnosisCode().isBlank())) {
            throw new BadRequestAlertException("At least one diagnosis is required.", ENTITY, "diagnosisRequired");
        }
        if (vm.serviceId() != null && vm.billingItemType() == BillingItemTypes.SERVICE) {
            requireService(vm.serviceId());
        }
        if (vm.excludedResult() != YesNoQuestion.YES && vm.excludedResult() != YesNoQuestion.NO) {
            throw new BadRequestAlertException("Excluded result must be Yes or No.", ENTITY, "invalidExcludedResult");
        }
    }

    private void applyPreApproval(CoveragePreApproval entity, CoveragePreApprovalVM vm) {
        entity.setApprovalScope(vm.approvalScope());
        entity.setFacilityId(vm.approvalScope() == CoverageApprovalScope.FACILITY ? requireFacility(vm.facilityId()).getId() : null);
        entity.setDepartmentId(vm.approvalScope() == CoverageApprovalScope.DEPARTMENT
                ? requireDepartment(vm.departmentId(), null).getId()
                : null);
        entity.setEncounterType(vm.approvalScope() == CoverageApprovalScope.FACILITY ? requireEncounterType(vm.encounterType()) : null);
        entity.setValueType(vm.valueType());
        entity.setLimitValue(vm.limitValue());
    }

    private void validatePreApproval(CoveragePreApprovalVM vm) {
        if (vm.approvalScope() == CoverageApprovalScope.FACILITY) {
            requireFacility(vm.facilityId());
            requireEncounterType(vm.encounterType());
        } else if (vm.approvalScope() == CoverageApprovalScope.DEPARTMENT) {
            requireDepartment(vm.departmentId(), null);
        }
        if (vm.valueType() == null) {
            throw new BadRequestAlertException("Value type is required.", ENTITY, "valueTypeRequired");
        }
        requirePositive(vm.limitValue(), "Limit value");
        if (vm.valueType() == InsuranceCoverageType.PERCENTAGE && vm.limitValue().compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestAlertException("Percentage limit cannot be greater than 100.", ENTITY, "invalidValue");
        }
    }

    private void validatePreApprovalItem(CoveragePreApprovalItemVM vm) {
        if (vm.itemType() == CoverageRuleTarget.CATEGORY) {
            requireCategory(vm.serviceCategory());
        }
        if (vm.itemType() == CoverageRuleTarget.SERVICE) {
            requireService(vm.serviceId());
        }
        if (vm.itemType() == CoverageRuleTarget.DIAGNOSIS
                && !Boolean.TRUE.equals(vm.allDiagnoses())
                && CoverageDiagnosisSelection.isEmpty(requestedDiagnosisIds(vm.diagnoses(), vm.diagnosisIds()), vm.diagnosisId())
                && (vm.diagnosisCode() == null || vm.diagnosisCode().isBlank())) {
            throw new BadRequestAlertException("At least one diagnosis is required.", ENTITY, "diagnosisRequired");
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

    private Long resolveCategoryItemId(BillingItemTypes category, Long itemId) {
        if (itemId == null) {
            return null;
        }
        if (category == BillingItemTypes.SERVICE) {
            return requireService(itemId).getId();
        }
        return itemId;
    }

    private String firstNonBlank(String first, String second) {
        return first != null ? first : second;
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

    private List<Long> resolveSelectedDiagnosisIds(
            boolean specific,
            Boolean allDiagnoses,
            List<CoverageDiagnosisRefVM> diagnoses,
            List<Long> diagnosisIds,
            Long diagnosisId,
            String diagnosisCode
    ) {
        if (!specific || Boolean.TRUE.equals(allDiagnoses)) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (Long id : requestedDiagnosisIds(diagnoses, diagnosisIds)) {
            ids.add(coverageLookupService.requireDiagnosis(id, null).getId());
        }
        if (ids.isEmpty() && (diagnosisId != null || (diagnosisCode != null && !diagnosisCode.isBlank()))) {
            ids.add(coverageLookupService.requireDiagnosis(diagnosisId, diagnosisCode).getId());
        }
        return CoverageDiagnosisSelection.unique(ids);
    }

    private List<Long> requestedDiagnosisIds(List<CoverageDiagnosisRefVM> diagnoses, List<Long> diagnosisIds) {
        List<Long> ids = new ArrayList<>();
        if (diagnoses != null) {
            for (CoverageDiagnosisRefVM diagnosis : diagnoses) {
                if (diagnosis == null) {
                    continue;
                }
                if (diagnosis.diagnosisId() != null) {
                    ids.add(diagnosis.diagnosisId());
                    continue;
                }
                if (diagnosis.diagnosisCode() != null && !diagnosis.diagnosisCode().isBlank()) {
                    ids.add(coverageLookupService.requireDiagnosis(null, diagnosis.diagnosisCode()).getId());
                }
            }
        }
        if (diagnosisIds != null) {
            for (Long id : diagnosisIds) {
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    private boolean allDiagnosesSelected(boolean diagnosisTarget, Boolean allDiagnoses, List<Long> diagnosisIds) {
        return diagnosisTarget && (Boolean.TRUE.equals(allDiagnoses) || diagnosisIds.isEmpty());
    }

    private List<CoverageDiagnosisRefVM> toDiagnosisRefs(List<Long> stored, Long fallback) {
        List<CoverageDiagnosisRefVM> refs = new ArrayList<>();
        for (Long id : CoverageDiagnosisSelection.ids(stored, fallback)) {
            ICDDiagnosis diagnosis = coverageLookupService.findDiagnosis(id);
            refs.add(new CoverageDiagnosisRefVM(
                    id,
                    coverageLookupService.diagnosisCode(diagnosis),
                    coverageLookupService.diagnosisName(diagnosis)
            ));
        }
        return refs;
    }

    private String diagnosisSummary(List<CoverageDiagnosisRefVM> diagnoses) {
        List<String> labels = new ArrayList<>();
        for (CoverageDiagnosisRefVM diagnosis : diagnoses) {
            if (diagnosis.diagnosisCode() != null && !diagnosis.diagnosisCode().isBlank()) {
                labels.add(diagnosis.diagnosisCode());
            } else if (diagnosis.diagnosisName() != null && !diagnosis.diagnosisName().isBlank()) {
                labels.add(diagnosis.diagnosisName());
            }
        }
        return labels.isEmpty() ? null : String.join(", ", labels);
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

    private CoverageDiscount requireOwnedDiscount(Long id, Long classId) {
        CoverageDiscount entity = requireDiscount(id);
        if (entity.getCoverageClass() == null || !entity.getCoverageClass().getId().equals(classId)) {
            throw new BadRequestAlertException("Discount does not belong to this class.", ENTITY, "classMismatch");
        }
        return entity;
    }

    private CoverageDiscount requireOwnedTpaDiscount(Long id, Long tpaId) {
        CoverageDiscount entity = requireDiscount(id);
        if (entity.getTpaDefinition() == null || !entity.getTpaDefinition().getId().equals(tpaId)) {
            throw new BadRequestAlertException("Discount does not belong to this TPA.", TPA_ENTITY, "tpaMismatch");
        }
        return entity;
    }

    private CoverageDiscount requireDiscount(Long id) {
        if (id == null) {
            throw new BadRequestAlertException("Discount id is required.", ENTITY, "idRequired");
        }
        return discountRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Discount was not found.", ENTITY, "notFound"));
    }

    private CoverageExclusion requireOwnedExclusion(Long id, Long classId) {
        CoverageExclusion entity = requireExclusion(id);
        if (entity.getCoverageClass() == null || !entity.getCoverageClass().getId().equals(classId)) {
            throw new BadRequestAlertException("Exclusion does not belong to this class.", ENTITY, "classMismatch");
        }
        return entity;
    }

    private CoverageExclusion requireOwnedTpaExclusion(Long id, Long tpaId) {
        CoverageExclusion entity = requireExclusion(id);
        if (entity.getTpaDefinition() == null || !entity.getTpaDefinition().getId().equals(tpaId)) {
            throw new BadRequestAlertException("Exclusion does not belong to this TPA.", TPA_ENTITY, "tpaMismatch");
        }
        return entity;
    }

    private CoverageExclusion requireExclusion(Long id) {
        if (id == null) {
            throw new BadRequestAlertException("Exclusion id is required.", ENTITY, "idRequired");
        }
        return exclusionRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Exclusion was not found.", ENTITY, "notFound"));
    }

    private CoveragePreApprovalItem requireOwnedPreApprovalItem(Long id, Long preApprovalId) {
        if (id == null) {
            throw new BadRequestAlertException("Pre-approval item id is required.", ENTITY, "idRequired");
        }
        CoveragePreApprovalItem entity = preApprovalItemRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Pre-approval item was not found.", ENTITY, "notFound"));
        if (entity.getPreApproval() == null || !entity.getPreApproval().getId().equals(preApprovalId)) {
            throw new BadRequestAlertException("Item does not belong to this pre-approval.", ENTITY, "preApprovalMismatch");
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

    private BillingItemTypes requireCategory(BillingItemTypes category) {
        if (category == null) {
            throw new BadRequestAlertException("Category is required.", ENTITY, "categoryRequired");
        }
        if (category == BillingItemTypes.PATHOLOGY) {
            throw new BadRequestAlertException(
                    "Category must be medication, laboratory, radiology, procedure, or service.",
                    ENTITY,
                    "invalidCategory"
            );
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
        List<CoverageDiagnosisRefVM> diagnoses = toDiagnosisRefs(entity.getDiagnosisIds(), entity.getDiagnosisId());
        CoverageDiagnosisRefVM first = diagnoses.isEmpty() ? null : diagnoses.get(0);
        return new CoverageTermVM(
                entity.getId(),
                entity.getTermType(),
                entity.getDiagnosisScope(),
                first == null ? null : first.diagnosisId(),
                first == null ? null : first.diagnosisCode(),
                first == null ? null : first.diagnosisName(),
                diagnoses,
                CoverageDiagnosisSelection.ids(entity.getDiagnosisIds(), entity.getDiagnosisId()),
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
        List<CoverageDiagnosisRefVM> diagnoses = toDiagnosisRefs(entity.getDiagnosisIds(), entity.getDiagnosisId());
        CoverageDiagnosisRefVM first = diagnoses.isEmpty() ? null : diagnoses.get(0);
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
                first == null ? null : first.diagnosisId(),
                first == null ? (Boolean.TRUE.equals(entity.getAllDiagnoses()) ? "ALL" : null) : first.diagnosisCode(),
                first == null
                        ? (Boolean.TRUE.equals(entity.getAllDiagnoses()) ? "All diagnoses" : null)
                        : diagnosisSummary(diagnoses),
                diagnoses,
                CoverageDiagnosisSelection.ids(entity.getDiagnosisIds(), entity.getDiagnosisId()),
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
                entity.getValueType(),
                entity.getLimitValue(),
                entity.getIsActive(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate()
        );
    }

    private CoveragePreApprovalItemVM toPreApprovalItemVm(CoveragePreApprovalItem entity) {
        List<CoverageDiagnosisRefVM> diagnoses = toDiagnosisRefs(entity.getDiagnosisIds(), entity.getDiagnosisId());
        CoverageDiagnosisRefVM first = diagnoses.isEmpty() ? null : diagnoses.get(0);
        String serviceName = blankToNull(entity.getItemName());
        if (serviceName == null && entity.getItemType() == CoverageRuleTarget.ALL) {
            serviceName = "All";
        } else if (serviceName == null && first != null) {
            serviceName = diagnosisSummary(diagnoses);
        }
        return new CoveragePreApprovalItemVM(
                entity.getId(),
                entity.getItemType(),
                entity.getServiceCategory(),
                entity.getServiceId(),
                null,
                serviceName,
                entity.getAllDiagnoses(),
                first == null ? null : first.diagnosisId(),
                first == null ? (Boolean.TRUE.equals(entity.getAllDiagnoses()) ? "ALL" : null) : first.diagnosisCode(),
                first == null
                        ? (entity.getItemType() == CoverageRuleTarget.ALL || Boolean.TRUE.equals(entity.getAllDiagnoses()) ? "All" : null)
                        : diagnosisSummary(diagnoses),
                diagnoses,
                CoverageDiagnosisSelection.ids(entity.getDiagnosisIds(), entity.getDiagnosisId()),
                entity.getIsActive(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate()
        );
    }
}
