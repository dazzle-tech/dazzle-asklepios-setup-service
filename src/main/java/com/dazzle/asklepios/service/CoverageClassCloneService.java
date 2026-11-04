package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageClass;
import com.dazzle.asklepios.domain.CoverageCopayment;
import com.dazzle.asklepios.domain.CoverageDiscount;
import com.dazzle.asklepios.domain.CoverageExclusion;
import com.dazzle.asklepios.domain.CoveragePreApproval;
import com.dazzle.asklepios.domain.CoveragePreApprovalItem;
import com.dazzle.asklepios.domain.CoverageTerm;
import com.dazzle.asklepios.domain.CoverageTermExclusion;
import com.dazzle.asklepios.domain.CoverageTermItem;
import com.dazzle.asklepios.domain.enumeration.CoverageClassCloneSection;
import com.dazzle.asklepios.repository.CoverageCopaymentRepository;
import com.dazzle.asklepios.repository.CoverageDiscountRepository;
import com.dazzle.asklepios.repository.CoverageExclusionRepository;
import com.dazzle.asklepios.repository.CoveragePreApprovalItemRepository;
import com.dazzle.asklepios.repository.CoveragePreApprovalRepository;
import com.dazzle.asklepios.repository.CoverageTermExclusionRepository;
import com.dazzle.asklepios.repository.CoverageTermItemRepository;
import com.dazzle.asklepios.repository.CoverageTermRepository;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageClassCloneVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageClassResponseVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageClassSaveVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Copies a coverage class into a new class of the same contract, with the rule tabs the user picks
 * (co-payment, coverages, coverage limit, cash limit, discount, exclusions, pre-approval) and their
 * details: limit readings and exclusions, and pre-approval items. Only active rules are copied.
 */
@Service
@Transactional
public class CoverageClassCloneService {

    private static final Logger LOG = LoggerFactory.getLogger(CoverageClassCloneService.class);

    private final CoverageClassService coverageClassService;
    private final CoverageCopaymentRepository copaymentRepository;
    private final CoverageTermRepository termRepository;
    private final CoverageTermItemRepository termItemRepository;
    private final CoverageTermExclusionRepository termExclusionRepository;
    private final CoverageDiscountRepository discountRepository;
    private final CoverageExclusionRepository exclusionRepository;
    private final CoveragePreApprovalRepository preApprovalRepository;
    private final CoveragePreApprovalItemRepository preApprovalItemRepository;

    public CoverageClassCloneService(
            CoverageClassService coverageClassService,
            CoverageCopaymentRepository copaymentRepository,
            CoverageTermRepository termRepository,
            CoverageTermItemRepository termItemRepository,
            CoverageTermExclusionRepository termExclusionRepository,
            CoverageDiscountRepository discountRepository,
            CoverageExclusionRepository exclusionRepository,
            CoveragePreApprovalRepository preApprovalRepository,
            CoveragePreApprovalItemRepository preApprovalItemRepository
    ) {
        this.coverageClassService = coverageClassService;
        this.copaymentRepository = copaymentRepository;
        this.termRepository = termRepository;
        this.termItemRepository = termItemRepository;
        this.termExclusionRepository = termExclusionRepository;
        this.discountRepository = discountRepository;
        this.exclusionRepository = exclusionRepository;
        this.preApprovalRepository = preApprovalRepository;
        this.preApprovalItemRepository = preApprovalItemRepository;
    }

    public CoverageClassResponseVM cloneClass(Long sourceClassId, CoverageClassCloneVM vm) {
        LOG.debug("Clone coverage class sourceId={} payload={}", sourceClassId, vm);
        CoverageClass source = coverageClassService.getEntity(sourceClassId);
        Long targetId = coverageClassService
                .create(new CoverageClassSaveVM(source.getCoverageContract().getId(), vm.name(), true))
                .id();

        if (Boolean.TRUE.equals(vm.copyPolicyLink()) && source.getInsuranceCompanyPolicy() != null) {
            coverageClassService.linkPolicy(targetId, source.getInsuranceCompanyPolicy().getId());
        }

        CoverageClass target = coverageClassService.getEntity(targetId);
        for (CoverageClassCloneSection section : CoverageClassCloneSection.values()) {
            if (vm.includes(section)) {
                copySection(section, source, target);
            }
        }

        LOG.info(
                "[COVERAGE_CLASS_CLONE] sourceId={} targetId={} sections={}",
                sourceClassId,
                targetId,
                vm.sections()
        );
        return coverageClassService.get(targetId);
    }

    private void copySection(CoverageClassCloneSection section, CoverageClass source, CoverageClass target) {
        switch (section) {
            case CO_PAYMENT -> copyCopayments(source, target);
            case COVERAGE, COVERAGE_LIMIT, CASH_LIMIT -> copyTerms(section, source, target);
            case DISCOUNT -> copyDiscounts(source, target);
            case EXCLUSION -> copyExclusions(source, target);
            case PRE_APPROVAL -> copyPreApprovals(source, target);
        }
    }

    private void copyCopayments(CoverageClass source, CoverageClass target) {
        copaymentRepository.saveAll(
                copaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(source.getId())
                        .stream()
                        .map(copayment -> CoverageCopayment.builder()
                                .coverageClass(target)
                                .facilityId(copayment.getFacilityId())
                                .allDepartments(copayment.getAllDepartments())
                                .departmentId(copayment.getDepartmentId())
                                .encounterType(copayment.getEncounterType())
                                .valueType(copayment.getValueType())
                                .valueAmount(copayment.getValueAmount())
                                .isActive(true)
                                .build())
                        .toList()
        );
    }

    private void copyTerms(CoverageClassCloneSection section, CoverageClass source, CoverageClass target) {
        for (CoverageTerm term : termRepository.findByCoverageClass_IdAndTermTypeAndIsActiveTrue(source.getId(), section.termType())) {
            CoverageTerm copy = termRepository.save(CoverageTerm.builder()
                    .coverageClass(target)
                    .termType(term.getTermType())
                    .diagnosisScope(term.getDiagnosisScope())
                    .diagnosisId(term.getDiagnosisId())
                    .diagnosisIds(copyOf(term.getDiagnosisIds()))
                    .facilityId(term.getFacilityId())
                    .allDepartments(term.getAllDepartments())
                    .departmentId(term.getDepartmentId())
                    .encounterType(term.getEncounterType())
                    .periodBasis(term.getPeriodBasis())
                    .coverageBasis(term.getCoverageBasis())
                    .valueType(term.getValueType())
                    .limitValue(term.getLimitValue())
                    .isActive(true)
                    .build());
            copyTermItems(term, copy);
            copyTermExclusions(term, copy);
        }
    }

    private void copyTermItems(CoverageTerm source, CoverageTerm target) {
        termItemRepository.saveAll(
                termItemRepository.findByCoverageTerm_IdAndIsActiveTrue(source.getId())
                        .stream()
                        .map(item -> CoverageTermItem.builder()
                                .coverageTerm(target)
                                .categoryScope(item.getCategoryScope())
                                .billingItemType(item.getBillingItemType())
                                .serviceId(item.getServiceId())
                                .subcategoryKey(item.getSubcategoryKey())
                                .itemName(item.getItemName())
                                .valueType(item.getValueType())
                                .limitValue(item.getLimitValue())
                                .isActive(true)
                                .build())
                        .toList()
        );
    }

    private void copyTermExclusions(CoverageTerm source, CoverageTerm target) {
        termExclusionRepository.saveAll(
                termExclusionRepository.findByCoverageTerm_IdAndIsActiveTrue(source.getId())
                        .stream()
                        .map(exclusion -> CoverageTermExclusion.builder()
                                .coverageTerm(target)
                                .categoryScope(exclusion.getCategoryScope())
                                .billingItemType(exclusion.getBillingItemType())
                                .subcategoryKey(exclusion.getSubcategoryKey())
                                .serviceId(exclusion.getServiceId())
                                .itemName(exclusion.getItemName())
                                .isActive(true)
                                .build())
                        .toList()
        );
    }

    private void copyDiscounts(CoverageClass source, CoverageClass target) {
        discountRepository.saveAll(
                discountRepository.findByCoverageClass_IdAndIsActiveTrue(source.getId())
                        .stream()
                        .map(discount -> CoverageDiscount.builder()
                                .coverageClass(target)
                                .targetType(discount.getTargetType())
                                .billingItemType(discount.getBillingItemType())
                                .serviceId(discount.getServiceId())
                                .subcategoryKey(discount.getSubcategoryKey())
                                .itemName(discount.getItemName())
                                .encounterType(discount.getEncounterType())
                                .discountType(discount.getDiscountType())
                                .discountValue(discount.getDiscountValue())
                                .isActive(true)
                                .build())
                        .toList()
        );
    }

    private void copyExclusions(CoverageClass source, CoverageClass target) {
        exclusionRepository.saveAll(
                exclusionRepository.findByCoverageClass_IdAndIsActiveTrue(source.getId())
                        .stream()
                        .map(exclusion -> CoverageExclusion.builder()
                                .coverageClass(target)
                                .exclusionType(exclusion.getExclusionType())
                                .billingItemType(exclusion.getBillingItemType())
                                .serviceId(exclusion.getServiceId())
                                .itemName(exclusion.getItemName())
                                .allDiagnoses(exclusion.getAllDiagnoses())
                                .diagnosisId(exclusion.getDiagnosisId())
                                .diagnosisIds(copyOf(exclusion.getDiagnosisIds()))
                                .encounterType(exclusion.getEncounterType())
                                .excludedResult(exclusion.getExcludedResult())
                                .isActive(true)
                                .build())
                        .toList()
        );
    }

    private void copyPreApprovals(CoverageClass source, CoverageClass target) {
        for (CoveragePreApproval preApproval : preApprovalRepository.findByCoverageClass_IdAndIsActiveTrue(source.getId())) {
            CoveragePreApproval copy = preApprovalRepository.save(CoveragePreApproval.builder()
                    .coverageClass(target)
                    .approvalScope(preApproval.getApprovalScope())
                    .facilityId(preApproval.getFacilityId())
                    .departmentId(preApproval.getDepartmentId())
                    .encounterType(preApproval.getEncounterType())
                    .valueType(preApproval.getValueType())
                    .limitValue(preApproval.getLimitValue())
                    .targetLevel(preApproval.getTargetLevel())
                    .source(preApproval.getSource())
                    .sourceId(preApproval.getSourceId())
                    .subcategoryKey(preApproval.getSubcategoryKey())
                    .isActive(true)
                    .build());
            copyPreApprovalItems(preApproval, copy);
        }
    }

    private void copyPreApprovalItems(CoveragePreApproval source, CoveragePreApproval target) {
        preApprovalItemRepository.saveAll(
                preApprovalItemRepository.findByPreApproval_IdAndIsActiveTrue(source.getId())
                        .stream()
                        .map(item -> CoveragePreApprovalItem.builder()
                                .preApproval(target)
                                .itemType(item.getItemType())
                                .serviceCategory(item.getServiceCategory())
                                .serviceId(item.getServiceId())
                                .itemName(item.getItemName())
                                .allDiagnoses(item.getAllDiagnoses())
                                .diagnosisId(item.getDiagnosisId())
                                .diagnosisIds(copyOf(item.getDiagnosisIds()))
                                .isActive(true)
                                .build())
                        .toList()
        );
    }

    private static List<Long> copyOf(List<Long> ids) {
        return ids == null ? new ArrayList<>() : new ArrayList<>(ids);
    }
}
