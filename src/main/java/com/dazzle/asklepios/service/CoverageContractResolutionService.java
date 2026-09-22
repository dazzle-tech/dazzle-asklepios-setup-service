package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageClass;
import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.CoverageCopayment;
import com.dazzle.asklepios.domain.CoverageDiscount;
import com.dazzle.asklepios.domain.CoverageExclusion;
import com.dazzle.asklepios.domain.CoveragePreApproval;
import com.dazzle.asklepios.domain.CoveragePreApprovalItem;
import com.dazzle.asklepios.domain.CoverageTerm;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.CoverageTermItem;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.domain.enumeration.CoverageApprovalScope;
import com.dazzle.asklepios.domain.enumeration.CoverageDiagnosisScope;
import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.CoverageTermType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.patient.YesNoQuestion;
import com.dazzle.asklepios.repository.CoverageClassRepository;
import com.dazzle.asklepios.repository.CoverageContractRepository;
import com.dazzle.asklepios.repository.CoverageCopaymentRepository;
import com.dazzle.asklepios.repository.CoverageDiscountRepository;
import com.dazzle.asklepios.repository.CoverageExclusionRepository;
import com.dazzle.asklepios.repository.CoveragePreApprovalItemRepository;
import com.dazzle.asklepios.repository.CoveragePreApprovalRepository;
import com.dazzle.asklepios.repository.CoverageTermItemRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.repository.CoverageTermRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.repository.TpaDefinitionRepository;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractResolveRequest;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractResolveResponse;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageCopaymentVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageDiscountVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageExclusionVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoveragePreApprovalReadingVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageReadingVM;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CoverageContractResolutionService {

    private final CoverageClassRepository coverageClassRepository;
    private final CoverageContractRepository coverageContractRepository;
    private final CoverageCopaymentRepository coverageCopaymentRepository;
    private final CoverageTermRepository coverageTermRepository;
    private final CoverageTermItemRepository coverageTermItemRepository;
    private final CoverageDiscountRepository coverageDiscountRepository;
    private final CoverageExclusionRepository coverageExclusionRepository;
    private final CoveragePreApprovalRepository coveragePreApprovalRepository;
    private final CoveragePreApprovalItemRepository coveragePreApprovalItemRepository;
    private final ServiceRepository serviceRepository;
    private final NphiesPayerRepository nphiesPayerRepository;
    private final TpaDefinitionRepository tpaDefinitionRepository;
    private final PriceListSetupRepository priceListSetupRepository;
    private final CoverageContractService coverageContractService;
    private final CoverageLookupService coverageLookupService;

    public CoverageContractResolutionService(
            CoverageClassRepository coverageClassRepository,
            CoverageContractRepository coverageContractRepository,
            CoverageCopaymentRepository coverageCopaymentRepository,
            CoverageTermRepository coverageTermRepository,
            CoverageTermItemRepository coverageTermItemRepository,
            CoverageDiscountRepository coverageDiscountRepository,
            CoverageExclusionRepository coverageExclusionRepository,
            CoveragePreApprovalRepository coveragePreApprovalRepository,
            CoveragePreApprovalItemRepository coveragePreApprovalItemRepository,
            ServiceRepository serviceRepository,
            NphiesPayerRepository nphiesPayerRepository,
            TpaDefinitionRepository tpaDefinitionRepository,
            PriceListSetupRepository priceListSetupRepository,
            CoverageContractService coverageContractService,
            CoverageLookupService coverageLookupService
    ) {
        this.coverageClassRepository = coverageClassRepository;
        this.coverageContractRepository = coverageContractRepository;
        this.coverageCopaymentRepository = coverageCopaymentRepository;
        this.coverageTermRepository = coverageTermRepository;
        this.coverageTermItemRepository = coverageTermItemRepository;
        this.coverageDiscountRepository = coverageDiscountRepository;
        this.coverageExclusionRepository = coverageExclusionRepository;
        this.coveragePreApprovalRepository = coveragePreApprovalRepository;
        this.coveragePreApprovalItemRepository = coveragePreApprovalItemRepository;
        this.serviceRepository = serviceRepository;
        this.nphiesPayerRepository = nphiesPayerRepository;
        this.tpaDefinitionRepository = tpaDefinitionRepository;
        this.priceListSetupRepository = priceListSetupRepository;
        this.coverageContractService = coverageContractService;
        this.coverageLookupService = coverageLookupService;
    }

    public CoverageContractResolveResponse resolve(CoverageContractResolveRequest request) {
        if (request == null) {
            return CoverageContractResolveResponse.unmatched("Coverage details are required.");
        }

        String policyNumber = normalize(request.policyNumber());
        if (policyNumber == null) {
            return CoverageContractResolveResponse.unmatched("Policy number is required to match a coverage contract.");
        }

        NphiesPayer insurancePayer = resolveInsurancePayer(request);
        if (insurancePayer == null) {
            return CoverageContractResolveResponse.unmatched("Insurance company was not found for this coverage.");
        }

        List<CoverageContract> candidates =
                coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(
                        insurancePayer.getId(),
                        policyNumber
                );
        if (candidates.isEmpty()) {
            return CoverageContractResolveResponse.unmatched(
                    "No active coverage contract matches this insurance policy."
            );
        }

        LocalDate asOfDate = request.asOfDate() == null ? LocalDate.now() : request.asOfDate();
        List<CoverageContract> inForce = candidates.stream()
                .filter(contract -> isInForce(contract, asOfDate))
                .toList();
        if (inForce.isEmpty()) {
            return CoverageContractResolveResponse.unmatched(
                    "Matched coverage contracts are outside the price list effective dates."
            );
        }

        Long tpaId = resolveTpaId(request, insurancePayer);
        MatchedCoverage matched = selectMatchedCoverage(inForce, request.className(), tpaId);
        CoverageContract selected = matched.contract();
        CoverageClass selectedClass = matched.coverageClass();
        Long classId = selectedClass == null ? null : selectedClass.getId();

        EncounterType encounterType = parseEncounterType(request.encounterType());
        CoverageCopayment copayment = selectCopayment(classId, encounterType);
        CoverageMatch coverageMatch = selectCoverage(classId, request, encounterType);
        CoverageMatch limitMatch = selectLimit(classId, request, encounterType);
        CoverageMatch cashLimitMatch = selectCashLimit(classId, request, encounterType);
        CoverageDiscountVM discount = selectDiscount(classId, tpaId, request, encounterType);
        CoverageExclusionVM exclusion = selectExclusion(classId, tpaId, request, encounterType);
        CoveragePreApprovalReadingVM preApproval = selectPreApproval(classId, tpaId, request, encounterType);

        return new CoverageContractResolveResponse(
                true,
                buildMatchReason(
                        selected,
                        selectedClass,
                        tpaId,
                        copayment,
                        encounterType,
                        coverageMatch,
                        limitMatch,
                        cashLimitMatch,
                        discount,
                        exclusion,
                        preApproval
                ),
                coverageContractService.toResponse(selected, selectedClass),
                copayment == null ? null : CoverageCopaymentVM.ofEntity(copayment),
                coverageMatch.configured(),
                coverageMatch.uncovered(),
                coverageMatch.reading(),
                limitMatch.reading(),
                cashLimitMatch.reading(),
                discount,
                exclusion,
                preApproval
        );
    }

    private NphiesPayer resolveInsurancePayer(CoverageContractResolveRequest request) {
        if (request.insurancePayerId() != null) {
            return nphiesPayerRepository.findById(request.insurancePayerId()).orElse(null);
        }
        String nphiesId = normalize(request.payerNphiesId());
        if (nphiesId == null) {
            return null;
        }
        return nphiesPayerRepository.findFirstByNphiesIdIgnoreCase(nphiesId).orElse(null);
    }

    private Long resolveTpaId(CoverageContractResolveRequest request, NphiesPayer insurancePayer) {
        if (request.tpaId() != null) {
            return tpaDefinitionRepository.findById(request.tpaId())
                    .filter(tpa -> Boolean.TRUE.equals(tpa.getIsActive()))
                    .map(TpaDefinition::getId)
                    .orElse(null);
        }

        String tpaName = normalize(request.tpaName());
        if (tpaName != null) {
            List<TpaDefinition> named = tpaDefinitionRepository.findByIsActiveTrueAndNameIgnoreCase(tpaName);
            if (named.size() == 1) {
                return named.get(0).getId();
            }
            if (!named.isEmpty() && insurancePayer.getId() != null) {
                return named.stream()
                        .filter(tpa -> isLinkedToPayer(tpa, insurancePayer.getId()))
                        .map(TpaDefinition::getId)
                        .findFirst()
                        .orElse(named.get(0).getId());
            }
        }

        List<TpaDefinition> linked = tpaDefinitionRepository.findByInsuranceCompanies_Id(insurancePayer.getId())
                .stream()
                .filter(tpa -> Boolean.TRUE.equals(tpa.getIsActive()))
                .toList();
        if (linked.size() == 1) {
            return linked.get(0).getId();
        }
        return null;
    }

    private boolean isLinkedToPayer(TpaDefinition tpa, Long nphiesPayerId) {
        if (tpa.getInsuranceCompanies() == null) {
            return false;
        }
        return tpa.getInsuranceCompanies().stream()
                .anyMatch(payer -> Objects.equals(payer.getId(), nphiesPayerId));
    }

    private boolean isInForce(CoverageContract contract, LocalDate asOfDate) {
        if (contract.getPriceListSetupId() == null) {
            return true;
        }
        PriceListSetup priceList = priceListSetupRepository.findById(contract.getPriceListSetupId()).orElse(null);
        if (priceList == null) {
            return true;
        }
        if (priceList.getEffectiveFrom() != null && asOfDate.isBefore(priceList.getEffectiveFrom())) {
            return false;
        }
        return priceList.getEffectiveTo() == null || !asOfDate.isAfter(priceList.getEffectiveTo());
    }

    private MatchedCoverage selectMatchedCoverage(
            List<CoverageContract> inForce,
            String requestedClassName,
            Long tpaId
    ) {
        Map<Long, CoverageContract> contractsById = new HashMap<>();
        for (CoverageContract contract : inForce) {
            contractsById.put(contract.getId(), contract);
        }
        List<CoverageClass> classes = coverageClassRepository.findByCoverageContract_IdInAndIsActiveTrue(contractsById.keySet());
        Map<Long, List<CoverageClass>> classesByContractId = new HashMap<>();
        for (CoverageClass coverageClass : classes) {
            Long contractId = coverageClass.getCoverageContract() == null ? null : coverageClass.getCoverageContract().getId();
            if (contractId != null && contractsById.containsKey(contractId)) {
                classesByContractId.computeIfAbsent(contractId, ignored -> new ArrayList<>()).add(coverageClass);
            }
        }

        CoverageContract selectedContract = inForce.stream()
                .max(Comparator.comparingInt(contract -> score(contract, hasMatchingClass(classesByContractId.get(contract.getId()), requestedClassName), tpaId)))
                .orElse(inForce.get(0));
        CoverageClass selectedClass = pickClass(classesByContractId.getOrDefault(selectedContract.getId(), List.of()), requestedClassName);
        return new MatchedCoverage(selectedContract, selectedClass);
    }

    private boolean hasMatchingClass(List<CoverageClass> classes, String requestedClassName) {
        if (classes == null || classes.isEmpty()) {
            return false;
        }
        return pickClass(classes, requestedClassName) != null && matchesClassName(pickClass(classes, requestedClassName).getName(), requestedClassName);
    }

    private CoverageClass pickClass(List<CoverageClass> classes, String requestedClassName) {
        if (classes == null || classes.isEmpty()) {
            return null;
        }
        if (requestedClassName != null && !requestedClassName.isBlank()) {
            return classes.stream()
                    .filter(coverageClass -> matchesClassName(coverageClass.getName(), requestedClassName))
                    .findFirst()
                    .orElse(null);
        }
        return classes.size() == 1 ? classes.get(0) : null;
    }

    private boolean matchesClassName(String className, String requestedClassName) {
        String requested = compactClassName(requestedClassName);
        String actual = compactClassName(className);
        if (requested == null || actual == null) {
            return false;
        }
        return actual.equals(requested) || actual.equals(requested.replace("class", ""));
    }

    private String compactClassName(String raw) {
        String normalized = normalize(raw);
        if (normalized == null) {
            return null;
        }
        return normalized.replace("class", "").replace("-", "").replace("_", "").replace(" ", "");
    }

    private int score(CoverageContract contract, boolean classMatched, Long tpaId) {
        int score = 100;
        if (classMatched) {
            score += 40;
        }
        if (tpaId != null && contract.getGuarantorType() == GuarantorType.TPA && tpaId.equals(contract.getCompanyId())) {
            score += 30;
        } else if (tpaId == null && contract.getGuarantorType() == GuarantorType.INSURANCE) {
            score += 10;
        }
        return score;
    }

    private CoverageCopayment selectCopayment(Long classId, EncounterType encounterType) {
        if (classId == null) {
            return null;
        }
        List<CoverageCopayment> rows =
                coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(classId);
        if (rows.isEmpty()) {
            return null;
        }
        if (encounterType != null && encounterType != EncounterType.ALL) {
            CoverageCopayment exact = rows.stream()
                    .filter(row -> row.getEncounterType() == encounterType)
                    .findFirst()
                    .orElse(null);
            if (exact != null) {
                return exact;
            }
        }
        return rows.stream()
                .filter(row -> row.getEncounterType() == EncounterType.ALL)
                .findFirst()
                .orElse(null);
    }

    private CoverageMatch selectCoverage(
            Long classId,
            CoverageContractResolveRequest request,
            EncounterType encounterType
    ) {
        return selectTermReading(classId, CoverageTermType.COVERAGE, request, encounterType, true);
    }

    private CoverageMatch selectLimit(
            Long classId,
            CoverageContractResolveRequest request,
            EncounterType encounterType
    ) {
        return selectTermReading(classId, CoverageTermType.LIMIT, request, encounterType, false);
    }

    private CoverageMatch selectCashLimit(
            Long classId,
            CoverageContractResolveRequest request,
            EncounterType encounterType
    ) {
        return selectTermReading(classId, CoverageTermType.CASH_LIMIT, request, encounterType, false);
    }

    private CoverageDiscountVM selectDiscount(
            Long classId,
            Long tpaId,
            CoverageContractResolveRequest request,
            EncounterType encounterType
    ) {
        if (request.billingItemType() == null || request.billingItemType().isBlank()) {
            return null;
        }
        BillingItemTypes billingItemType = parseBillingItemType(request.billingItemType());
        if (billingItemType == null) {
            return null;
        }

        CoverageDiscount best = null;
        int bestScore = -1;
        for (ScoredDiscount candidate : scoredDiscounts(classId, tpaId)) {
            int score = discountMatchScore(
                    candidate.discount(),
                    candidate.fromContract(),
                    billingItemType,
                    request.catalogItemId(),
                    encounterType
            );
            if (score > bestScore) {
                bestScore = score;
                best = candidate.discount();
            }
        }
        return best == null ? null : toDiscountVm(best);
    }

    private CoverageExclusionVM selectExclusion(
            Long classId,
            Long tpaId,
            CoverageContractResolveRequest request,
            EncounterType encounterType
    ) {
        if (request.billingItemType() == null || request.billingItemType().isBlank()) {
            return null;
        }
        BillingItemTypes billingItemType = parseBillingItemType(request.billingItemType());
        if (billingItemType == null) {
            return null;
        }

        Set<Long> diagnosisIds = toIdSet(request.diagnosisIds());
        CoverageExclusion best = null;
        int bestScore = -1;
        for (ScoredExclusion candidate : scoredExclusions(classId, tpaId)) {
            int score = exclusionMatchScore(
                    candidate.exclusion(),
                    candidate.fromContract(),
                    billingItemType,
                    request.catalogItemId(),
                    encounterType,
                    diagnosisIds
            );
            if (score > bestScore) {
                bestScore = score;
                best = candidate.exclusion();
            }
        }
        return best == null ? null : toExclusionVm(best);
    }

    private CoveragePreApprovalReadingVM selectPreApproval(
            Long classId,
            Long tpaId,
            CoverageContractResolveRequest request,
            EncounterType encounterType
    ) {
        if (request.billingItemType() == null || request.billingItemType().isBlank()) {
            return null;
        }
        BillingItemTypes billingItemType = parseBillingItemType(request.billingItemType());
        if (billingItemType == null) {
            return null;
        }

        Set<Long> diagnosisIds = toIdSet(request.diagnosisIds());
        ServiceCategory serviceCategory = resolveServiceCategory(request, billingItemType);
        CoveragePreApprovalReadingVM best = null;
        int bestScore = -1;
        for (ScoredPreApproval header : scoredPreApprovals(classId, tpaId)) {
            int headerScore = preApprovalHeaderScore(header.preApproval(), header.fromContract(), request, encounterType);
            if (headerScore < 0) {
                continue;
            }
            List<CoveragePreApprovalItem> items =
                    coveragePreApprovalItemRepository.findByPreApproval_IdAndIsActiveTrue(header.preApproval().getId());
            if (items.isEmpty()) {
                continue;
            }
            for (CoveragePreApprovalItem item : items) {
                int itemScore = preApprovalItemScore(
                        item,
                        billingItemType,
                        request.catalogItemId(),
                        serviceCategory,
                        diagnosisIds
                );
                if (itemScore < 0) {
                    continue;
                }
                int score = headerScore * 1000 + itemScore;
                if (score > bestScore) {
                    bestScore = score;
                    best = toPreApprovalReading(header.preApproval(), item);
                }
            }
        }
        return best;
    }

    private List<ScoredPreApproval> scoredPreApprovals(Long classId, Long tpaId) {
        List<ScoredPreApproval> rows = new ArrayList<>();
        if (classId != null) {
            for (CoveragePreApproval preApproval : coveragePreApprovalRepository.findByCoverageClass_IdAndIsActiveTrue(classId)) {
                rows.add(new ScoredPreApproval(preApproval, true));
            }
        }
        if (tpaId != null) {
            for (CoveragePreApproval preApproval : coveragePreApprovalRepository.findByTpaDefinition_IdAndIsActiveTrue(tpaId)) {
                rows.add(new ScoredPreApproval(preApproval, false));
            }
        }
        return rows;
    }

    private int preApprovalHeaderScore(
            CoveragePreApproval preApproval,
            boolean fromContract,
            CoverageContractResolveRequest request,
            EncounterType encounterType
    ) {
        CoverageApprovalScope scope = preApproval.getApprovalScope();
        int scopeScore;
        EncounterType headerEncounter = preApproval.getEncounterType();
        if (scope == CoverageApprovalScope.DEPARTMENT) {
            if (preApproval.getDepartmentId() == null
                    || request.departmentId() == null
                    || !preApproval.getDepartmentId().equals(request.departmentId())) {
                return -1;
            }
            scopeScore = 200;
        } else if (scope == CoverageApprovalScope.FACILITY) {
            if (preApproval.getFacilityId() == null
                    || request.facilityId() == null
                    || !preApproval.getFacilityId().equals(request.facilityId())) {
                return -1;
            }
            if (headerEncounter != null && headerEncounter != EncounterType.ALL) {
                if (encounterType == null || encounterType != headerEncounter) {
                    return -1;
                }
            }
            scopeScore = 100;
        } else {
            return -1;
        }
        int encounterScore = headerEncounter != null && headerEncounter != EncounterType.ALL ? 20 : 5;
        int sourceScore = fromContract ? 10 : 0;
        return scopeScore * 10 + encounterScore + sourceScore;
    }

    private int preApprovalItemScore(
            CoveragePreApprovalItem item,
            BillingItemTypes billingItemType,
            Long catalogItemId,
            ServiceCategory serviceCategory,
            Set<Long> diagnosisIds
    ) {
        CoverageRuleTarget type = item.getItemType();
        if (type == CoverageRuleTarget.SERVICE) {
            if (catalogItemId != null && catalogItemId.equals(item.getServiceId())) {
                return 400;
            }
            return -1;
        }
        if (type == CoverageRuleTarget.DIAGNOSIS) {
            if (Boolean.TRUE.equals(item.getAllDiagnoses()) || item.getDiagnosisId() == null) {
                return diagnosisIds.isEmpty() ? -1 : 200;
            }
            if (coverageLookupService.ruleCoversAnyDiagnosis(item.getDiagnosisId(), diagnosisIds)) {
                return 350;
            }
            return -1;
        }
        if (type == CoverageRuleTarget.CATEGORY) {
            if (serviceCategory != null && serviceCategory == item.getServiceCategory()) {
                return 300;
            }
            return -1;
        }
        return billingItemType == null ? -1 : 100;
    }

    private ServiceCategory resolveServiceCategory(
            CoverageContractResolveRequest request,
            BillingItemTypes billingItemType
    ) {
        if (billingItemType != BillingItemTypes.SERVICE || request.catalogItemId() == null) {
            return null;
        }
        return serviceRepository.findById(request.catalogItemId())
                .map(ServiceSetup::getCategory)
                .orElse(null);
    }

    private CoveragePreApprovalReadingVM toPreApprovalReading(
            CoveragePreApproval preApproval,
            CoveragePreApprovalItem item
    ) {
        return new CoveragePreApprovalReadingVM(
                preApproval.getId(),
                item.getId(),
                preApproval.getApprovalScope(),
                preApproval.getFacilityId(),
                preApproval.getDepartmentId(),
                preApproval.getEncounterType(),
                item.getItemType(),
                item.getServiceCategory(),
                item.getServiceId(),
                item.getAllDiagnoses(),
                item.getDiagnosisId()
        );
    }

    private List<ScoredExclusion> scoredExclusions(Long classId, Long tpaId) {
        List<ScoredExclusion> rows = new ArrayList<>();
        if (classId != null) {
            for (CoverageExclusion exclusion : coverageExclusionRepository.findByCoverageClass_IdAndIsActiveTrue(classId)) {
                rows.add(new ScoredExclusion(exclusion, true));
            }
        }
        if (tpaId != null) {
            for (CoverageExclusion exclusion : coverageExclusionRepository.findByTpaDefinition_IdAndIsActiveTrue(tpaId)) {
                rows.add(new ScoredExclusion(exclusion, false));
            }
        }
        return rows;
    }

    private int exclusionMatchScore(
            CoverageExclusion exclusion,
            boolean fromContract,
            BillingItemTypes billingItemType,
            Long catalogItemId,
            EncounterType encounterType,
            Set<Long> diagnosisIds
    ) {
        if (exclusion.getExcludedResult() == null || exclusion.getExcludedResult() == YesNoQuestion.NOT_YET_DETERMINED) {
            return -1;
        }
        EncounterType exclusionEncounter = exclusion.getEncounterType();
        if (exclusionEncounter != null && exclusionEncounter != EncounterType.ALL) {
            if (encounterType == null || encounterType != exclusionEncounter) {
                return -1;
            }
        }
        int targetScore = exclusionTargetScore(exclusion, billingItemType, catalogItemId, diagnosisIds);
        if (targetScore < 0) {
            return -1;
        }
        int encounterScore = exclusionEncounter != null && exclusionEncounter != EncounterType.ALL ? 20 : 5;
        int sourceScore = fromContract ? 10 : 0;
        return targetScore * 1000 + encounterScore * 10 + sourceScore;
    }

    private int exclusionTargetScore(
            CoverageExclusion exclusion,
            BillingItemTypes billingItemType,
            Long catalogItemId,
            Set<Long> diagnosisIds
    ) {
        CoverageRuleTarget type = exclusion.getExclusionType();
        if (type == CoverageRuleTarget.SERVICE) {
            if (catalogItemId != null && catalogItemId.equals(exclusion.getServiceId())) {
                return 400;
            }
            return -1;
        }
        if (type == CoverageRuleTarget.DIAGNOSIS) {
            if (Boolean.TRUE.equals(exclusion.getAllDiagnoses()) || exclusion.getDiagnosisId() == null) {
                return diagnosisIds.isEmpty() ? -1 : 200;
            }
            if (coverageLookupService.ruleCoversAnyDiagnosis(exclusion.getDiagnosisId(), diagnosisIds)) {
                return 350;
            }
            return -1;
        }
        if (type == CoverageRuleTarget.CATEGORY) {
            if (exclusion.getBillingItemType() == billingItemType) {
                return 300;
            }
            return -1;
        }
        if (exclusion.getServiceId() != null) {
            if (catalogItemId != null && catalogItemId.equals(exclusion.getServiceId())) {
                return 400;
            }
            return -1;
        }
        if (exclusion.getBillingItemType() != null) {
            if (exclusion.getBillingItemType() == billingItemType) {
                return 300;
            }
            return -1;
        }
        return 100;
    }

    private CoverageExclusionVM toExclusionVm(CoverageExclusion exclusion) {
        return new CoverageExclusionVM(
                exclusion.getId(),
                exclusion.getExclusionType(),
                exclusion.getBillingItemType(),
                exclusion.getServiceId(),
                null,
                exclusion.getItemName(),
                exclusion.getAllDiagnoses(),
                exclusion.getDiagnosisId(),
                null,
                null,
                exclusion.getEncounterType(),
                exclusion.getExcludedResult(),
                exclusion.getIsActive(),
                exclusion.getCreatedDate(),
                exclusion.getLastModifiedDate()
        );
    }

    private List<ScoredDiscount> scoredDiscounts(Long classId, Long tpaId) {
        List<ScoredDiscount> rows = new ArrayList<>();
        if (classId != null) {
            for (CoverageDiscount discount : coverageDiscountRepository.findByCoverageClass_IdAndIsActiveTrue(classId)) {
                rows.add(new ScoredDiscount(discount, true));
            }
        }
        if (tpaId != null) {
            for (CoverageDiscount discount : coverageDiscountRepository.findByTpaDefinition_IdAndIsActiveTrue(tpaId)) {
                rows.add(new ScoredDiscount(discount, false));
            }
        }
        return rows;
    }

    private int discountMatchScore(
            CoverageDiscount discount,
            boolean fromContract,
            BillingItemTypes billingItemType,
            Long catalogItemId,
            EncounterType encounterType
    ) {
        EncounterType discountEncounter = discount.getEncounterType();
        if (discountEncounter != null && discountEncounter != EncounterType.ALL) {
            if (encounterType == null || encounterType != discountEncounter) {
                return -1;
            }
        }
        int targetScore = discountTargetScore(discount, billingItemType, catalogItemId);
        if (targetScore < 0) {
            return -1;
        }
        int encounterScore = discountEncounter != null && discountEncounter != EncounterType.ALL ? 20 : 5;
        int sourceScore = fromContract ? 10 : 0;
        return targetScore * 1000 + encounterScore * 10 + sourceScore;
    }

    private int discountTargetScore(
            CoverageDiscount discount,
            BillingItemTypes billingItemType,
            Long catalogItemId
    ) {
        if (discount.getServiceId() != null || discount.getTargetType() == CoverageRuleTarget.SERVICE) {
            if (catalogItemId != null && catalogItemId.equals(discount.getServiceId())) {
                return 300;
            }
            return -1;
        }
        if (discount.getBillingItemType() != null || discount.getTargetType() == CoverageRuleTarget.CATEGORY) {
            if (discount.getBillingItemType() == billingItemType) {
                return 200;
            }
            return -1;
        }
        return 100;
    }

    private CoverageDiscountVM toDiscountVm(CoverageDiscount discount) {
        return new CoverageDiscountVM(
                discount.getId(),
                discount.getTargetType(),
                discount.getBillingItemType(),
                discount.getServiceId(),
                null,
                discount.getItemName(),
                discount.getEncounterType(),
                discount.getDiscountType(),
                discount.getDiscountValue(),
                discount.getIsActive(),
                discount.getCreatedDate(),
                discount.getLastModifiedDate()
        );
    }

    private CoverageMatch selectTermReading(
            Long classId,
            CoverageTermType termType,
            CoverageContractResolveRequest request,
            EncounterType encounterType,
            boolean missIsUncovered
    ) {
        if (classId == null) {
            return CoverageMatch.notConfigured();
        }
        List<CoverageTerm> terms = coverageTermRepository.findByCoverageClass_IdAndTermTypeAndIsActiveTrue(
                classId,
                termType
        );
        if (terms.isEmpty()) {
            return CoverageMatch.notConfigured();
        }
        if (request.billingItemType() == null || request.billingItemType().isBlank()) {
            return missIsUncovered
                    ? CoverageMatch.configuredWithoutEvaluation()
                    : CoverageMatch.notConfigured();
        }

        BillingItemTypes billingItemType = parseBillingItemType(request.billingItemType());
        if (billingItemType == null) {
            return missIsUncovered
                    ? CoverageMatch.configuredWithoutEvaluation()
                    : CoverageMatch.notConfigured();
        }

        Set<Long> diagnosisIds = toIdSet(request.diagnosisIds());
        CoverageReadingVM bestReading = null;
        int bestScore = -1;

        for (CoverageTerm term : terms) {
            int headerScore = headerMatchScore(term, request.facilityId(), request.departmentId(), encounterType, diagnosisIds);
            if (headerScore < 0) {
                continue;
            }
            List<CoverageTermItem> items = coverageTermItemRepository.findByCoverageTerm_IdAndIsActiveTrue(term.getId());
            if (items.isEmpty()) {
                int score = headerScore * 1000 + 10;
                if (score > bestScore) {
                    bestScore = score;
                    bestReading = toReading(term, null);
                }
                continue;
            }
            for (CoverageTermItem item : items) {
                int itemScore = itemMatchScore(item, billingItemType, request.catalogItemId());
                if (itemScore < 0) {
                    continue;
                }
                int score = headerScore * 1000 + itemScore;
                if (score > bestScore) {
                    bestScore = score;
                    bestReading = toReading(term, item);
                }
            }
        }

        if (bestReading == null) {
            return missIsUncovered ? CoverageMatch.uncoveredItem() : CoverageMatch.notConfigured();
        }
        return CoverageMatch.covered(bestReading);
    }

    private CoverageReadingVM toReading(CoverageTerm term, CoverageTermItem item) {
        if (item == null) {
            return new CoverageReadingVM(
                    term.getId(),
                    null,
                    CoverageRuleTarget.ALL,
                    null,
                    null,
                    term.getValueType(),
                    term.getLimitValue(),
                    term.getPeriodBasis(),
                    term.getCoverageBasis()
            );
        }
        return new CoverageReadingVM(
                term.getId(),
                item.getId(),
                item.getCategoryScope(),
                item.getBillingItemType(),
                item.getServiceId(),
                item.getValueType(),
                item.getLimitValue(),
                term.getPeriodBasis(),
                term.getCoverageBasis()
        );
    }

    private int headerMatchScore(
            CoverageTerm term,
            Long facilityId,
            Long departmentId,
            EncounterType encounterType,
            Set<Long> diagnosisIds
    ) {
        if (facilityId != null && !facilityId.equals(term.getFacilityId())) {
            return -1;
        }
        if (Boolean.TRUE.equals(term.getAllDepartments()) || term.getDepartmentId() == null) {
            // all departments
        } else if (departmentId == null || !departmentId.equals(term.getDepartmentId())) {
            return -1;
        }
        EncounterType termEncounter = term.getEncounterType();
        if (termEncounter != null && termEncounter != EncounterType.ALL) {
            if (encounterType == null || encounterType != termEncounter) {
                return -1;
            }
        }
        if (term.getDiagnosisScope() == CoverageDiagnosisScope.SPECIFIC_DIAGNOSIS) {
            if (term.getDiagnosisId() == null || !coverageLookupService.ruleCoversAnyDiagnosis(term.getDiagnosisId(), diagnosisIds)) {
                return -1;
            }
        }

        int score = 0;
        if (term.getDiagnosisScope() == CoverageDiagnosisScope.SPECIFIC_DIAGNOSIS) {
            score += 40;
        } else {
            score += 10;
        }
        if (!Boolean.TRUE.equals(term.getAllDepartments()) && term.getDepartmentId() != null) {
            score += 30;
        } else {
            score += 10;
        }
        if (termEncounter != null && termEncounter != EncounterType.ALL) {
            score += 20;
        } else {
            score += 5;
        }
        return score;
    }

    private int itemMatchScore(CoverageTermItem item, BillingItemTypes billingItemType, Long catalogItemId) {
        if (item.getBillingItemType() == null) {
            return 100;
        }
        if (item.getBillingItemType() != billingItemType) {
            return -1;
        }
        if (item.getServiceId() != null) {
            if (catalogItemId != null && catalogItemId.equals(item.getServiceId())) {
                return 300;
            }
            return -1;
        }
        return 200;
    }

    private Set<Long> toIdSet(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        Set<Long> ids = new HashSet<>();
        for (Long value : values) {
            if (value != null) {
                ids.add(value);
            }
        }
        return ids;
    }

    private BillingItemTypes parseBillingItemType(String raw) {
        String normalized = normalize(raw);
        if (normalized == null) {
            return null;
        }
        for (BillingItemTypes value : BillingItemTypes.values()) {
            if (value.name().equalsIgnoreCase(normalized)) {
                return value;
            }
        }
        return null;
    }

    private String buildMatchReason(
            CoverageContract contract,
            CoverageClass coverageClass,
            Long tpaId,
            CoverageCopayment copayment,
            EncounterType encounterType,
            CoverageMatch coverageMatch,
            CoverageMatch limitMatch,
            CoverageMatch cashLimitMatch,
            CoverageDiscountVM discount,
            CoverageExclusionVM exclusion,
            CoveragePreApprovalReadingVM preApproval
    ) {
        StringBuilder reason = new StringBuilder("Matched policy ")
                .append(contract.getPolicyNumber());
        if (coverageClass != null && coverageClass.getName() != null && !coverageClass.getName().isBlank()) {
            reason.append(" and class ").append(coverageClass.getName());
        }
        if (tpaId != null && contract.getGuarantorType() == GuarantorType.TPA) {
            reason.append(" on the TPA contract");
        }
        if (coverageMatch.uncovered()) {
            reason.append(". Item is not covered by Coverages");
        } else if (coverageMatch.reading() != null) {
            reason.append(". Coverage reading applied");
        }
        if (limitMatch.reading() != null) {
            reason.append(". Coverage Limit reading applied");
        }
        if (cashLimitMatch.reading() != null) {
            reason.append(". Cash Limit reading applied");
        }
        if (discount != null) {
            reason.append(". Discount applied");
        }
        if (exclusion != null && exclusion.excludedResult() == YesNoQuestion.YES) {
            reason.append(". Item is excluded from coverage");
        } else if (exclusion != null && exclusion.excludedResult() == YesNoQuestion.NO) {
            reason.append(". Exclusion override keeps the item covered");
        }
        if (preApproval != null) {
            reason.append(". Pre-approval is required");
        }
        if (copayment == null) {
            reason.append(". No co-payment rule for this encounter type");
            if (encounterType != null) {
                reason.append(" (").append(encounterType).append(")");
            }
            reason.append(".");
            return reason.toString();
        }
        reason.append(". Co-payment applied from ")
                .append(copayment.getEncounterType())
                .append(" rule.");
        return reason.toString();
    }

    private record CoverageMatch(
            boolean configured,
            boolean uncovered,
            CoverageReadingVM reading
    ) {
        static CoverageMatch notConfigured() {
            return new CoverageMatch(false, false, null);
        }

        static CoverageMatch configuredWithoutEvaluation() {
            return new CoverageMatch(true, false, null);
        }

        static CoverageMatch uncoveredItem() {
            return new CoverageMatch(true, true, null);
        }

        static CoverageMatch covered(CoverageReadingVM reading) {
            return new CoverageMatch(true, false, reading);
        }
    }

    private record ScoredDiscount(CoverageDiscount discount, boolean fromContract) {}

    private record ScoredExclusion(CoverageExclusion exclusion, boolean fromContract) {}

    private record ScoredPreApproval(CoveragePreApproval preApproval, boolean fromContract) {}

    private record MatchedCoverage(CoverageContract contract, CoverageClass coverageClass) {}

    private EncounterType parseEncounterType(String raw) {
        String normalized = normalize(raw);
        if (normalized == null) {
            return null;
        }
        for (EncounterType value : EncounterType.values()) {
            if (value.name().equalsIgnoreCase(normalized)) {
                return value;
            }
        }
        return null;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
