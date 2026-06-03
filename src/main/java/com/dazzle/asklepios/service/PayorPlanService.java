package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.PayorPlan;
import com.dazzle.asklepios.domain.PayorPlanItem;
import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.repository.BrandMedicationRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PayorPlanItemRepository;
import com.dazzle.asklepios.repository.PayorPlanRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.payorplan.PayorPlanItemSaveVM;
import com.dazzle.asklepios.web.rest.vm.payorplan.PayorPlanItemUpdateVM;
import com.dazzle.asklepios.web.rest.vm.payorplan.PayorPlanSaveVM;
import com.dazzle.asklepios.web.rest.vm.payorplan.PayorPlanUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.dazzle.asklepios.domain.PayorPlanCoverageClass;
import com.dazzle.asklepios.domain.enumeration.CoverageClassType;
import com.dazzle.asklepios.domain.enumeration.CoverageType;
import com.dazzle.asklepios.repository.PayorPlanCoverageClassRepository;

import java.util.List;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
@Transactional
public class PayorPlanService {

    private static final Logger LOG = LoggerFactory.getLogger(PayorPlanService.class);

    private final PayorPlanRepository planRepo;
    private final PayorPlanItemRepository itemRepo;
    private final PayorPlanCoverageClassRepository coverageClassRepo;

    private final BrandMedicationRepository brandMedicationRepo;
    private final DiagnosticTestRepository diagnosticTestRepo;
    private final ServiceRepository serviceRepo;
    private final ProcedureRepository procedureRepo;

    public PayorPlanService(
            PayorPlanRepository planRepo,
            PayorPlanItemRepository itemRepo,
            PayorPlanCoverageClassRepository coverageClassRepo,
            BrandMedicationRepository brandMedicationRepo,
            DiagnosticTestRepository diagnosticTestRepo,
            ServiceRepository serviceRepo,
            ProcedureRepository procedureRepo
    ) {
        this.planRepo = planRepo;
        this.itemRepo = itemRepo;
        this.coverageClassRepo = coverageClassRepo;
        this.brandMedicationRepo = brandMedicationRepo;
        this.diagnosticTestRepo = diagnosticTestRepo;
        this.serviceRepo = serviceRepo;
        this.procedureRepo = procedureRepo;

    }

    // ---------------- PLAN CRUD ----------------

    public PayorPlan createPlan(PayorPlanSaveVM vm) {

        PayorPlan p = PayorPlan.builder()
                .payorId(vm.payorId())
                .name(vm.name())
                .planType(vm.planType())
                .networkId(vm.networkId())
                .coverageType(vm.coverageType())
                .payerNphiesId(vm.payerNphiesId())
                .waseelPlanId(vm.waseelPlanId())
                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .build();

        return planRepo.save(p);
    }

    public PayorPlan updatePlan(PayorPlanUpdateVM vm) {

        PayorPlan existing = planRepo.findById(vm.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound",
                        "payorPlan",
                        "Plan not found."
                ));

        existing.setPayorId(vm.payorId());
        existing.setName(vm.name());
        existing.setPlanType(vm.planType());
        existing.setNetworkId(vm.networkId());
        existing.setCoverageType(vm.coverageType());
        existing.setPayerNphiesId(vm.payerNphiesId());
        existing.setWaseelPlanId(vm.waseelPlanId());
        existing.setIsActive(
                vm.isActive() != null ? vm.isActive() : existing.getIsActive()
        );

        return planRepo.save(existing);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<PayorPlan> findOnePlan(Long id) {
        return planRepo.findById(id);
    }

    public void deletePlan(Long id) {

        itemRepo.deleteByPlan_Id(id);

        planRepo.deleteById(id);
    }

    public java.util.Optional<PayorPlan> togglePlanActive(Long id) {

        return planRepo.findById(id)
                .map(plan -> {

                    plan.setIsActive(
                            !Boolean.TRUE.equals(plan.getIsActive())
                    );

                    return planRepo.save(plan);
                });
    }

    // ---------------- PLAN LISTS ----------------

    @Transactional(readOnly = true)
    public Page<PayorPlan> getAllPlans(Pageable pageable) {
        return planRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PayorPlan> getAllActivePlans(Pageable pageable) {
        return planRepo.findByIsActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PayorPlan> getAllByPayor(Long payorId, Pageable pageable) {
        return planRepo.findByPayorId(payorId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PayorPlan> getAllActiveByPayor(Long payorId, Pageable pageable) {
        return planRepo.findByPayorIdAndIsActiveTrue(payorId, pageable);
    }

    // ---------------- ITEMS CRUD ----------------

    public PayorPlanItem createItem(PayorPlanItemSaveVM vm) {

        PayorPlan plan = planRepo.findById(vm.planId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "planNotFound",
                        "payorPlanItem",
                        "Plan not found."
                ));

        PayorPlanItem item = PayorPlanItem.builder()
                .plan(plan)
                .itemType(vm.itemType())
                .amount(vm.amount())
                .coverageType(vm.coverageType())
                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .preAuthorization(
                        vm.preAuthorization() != null
                                ? vm.preAuthorization()
                                : false
                )
                .build();

        applyItemReference(
                item,
                vm.itemType(),
                vm.brandMedicationId(),
                vm.diagnosticTestId(),
                vm.serviceId(),
                vm.procedureId()
        );

        return itemRepo.save(item);
    }

    public PayorPlanItem updateItem(PayorPlanItemUpdateVM vm) {

        PayorPlanItem existing = itemRepo.findById(vm.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound",
                        "payorPlanItem",
                        "Plan item not found."
                ));

        PayorPlan plan = planRepo.findById(vm.planId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "planNotFound",
                        "payorPlanItem",
                        "Plan not found."
                ));

        existing.setPlan(plan);
        existing.setItemType(vm.itemType());
        existing.setAmount(vm.amount());
        existing.setCoverageType(vm.coverageType());

        existing.setIsActive(
                vm.isActive() != null
                        ? vm.isActive()
                        : existing.getIsActive()
        );

        existing.setPreAuthorization(
                vm.preAuthorization() != null
                        ? vm.preAuthorization()
                        : existing.getPreAuthorization()
        );

        applyItemReference(
                existing,
                vm.itemType(),
                vm.brandMedicationId(),
                vm.diagnosticTestId(),
                vm.serviceId(),
                vm.procedureId()
        );

        return itemRepo.save(existing);
    }

    public void deleteItem(Long id) {
        itemRepo.deleteById(id);
    }

    public java.util.Optional<PayorPlanItem> toggleItemActive(Long id) {

        return itemRepo.findById(id)
                .map(item -> {

                    item.setIsActive(
                            !Boolean.TRUE.equals(item.getIsActive())
                    );

                    return itemRepo.save(item);
                });
    }

    @Transactional(readOnly = true)
    public Page<PayorPlanItem> getItemsByPlan(Long planId, Pageable pageable) {
        return itemRepo.findByPlan_Id(planId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PayorPlanItem> getActiveItemsByPlan(Long planId, Pageable pageable) {
        return itemRepo.findByPlan_IdAndIsActiveTrue(planId, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<PayorPlan> findCchiPlanByPayorAndWaseelPlanId(
            Long payorId,
            String waseelPlanId
    ) {
        if (payorId == null || isBlank(waseelPlanId)) {
            return Optional.empty();
        }

        return planRepo.findFirstByPayorIdAndWaseelPlanIdAndIsActiveTrue(
                payorId,
                waseelPlanId.trim()
        );
    }


    @Transactional(readOnly = true)
    public Optional<PayorPlan> findCchiPlanByMatch(
            Long payorId,
            String coverageType,
            String networkId,
            String policyClassName
    ) {
        if (payorId == null) {
            return Optional.empty();
        }

        String cleanCoverageType = clean(coverageType);
        String cleanNetworkId = clean(networkId);
        String cleanPolicyClassName = clean(policyClassName);

        if (isBlank(cleanCoverageType) && isBlank(cleanNetworkId) && isBlank(cleanPolicyClassName)) {
            return Optional.empty();
        }

        CoverageType coverageTypeEnum = parseCoverageType(cleanCoverageType);

        List<PayorPlan> candidatePlans;

        if (!isBlank(cleanNetworkId)) {
            candidatePlans = planRepo.findByPayorIdAndCoverageTypeAndNetworkIdAndIsActiveTrue(
                    payorId,
                    coverageTypeEnum,
                    cleanNetworkId
            );
        } else {
            candidatePlans = planRepo.findByPayorIdAndCoverageTypeAndIsActiveTrue(
                    payorId,
                    coverageTypeEnum
            );
        }

        if (candidatePlans.isEmpty()) {
            return Optional.empty();
        }

        if (isBlank(cleanPolicyClassName)) {
            return Optional.of(candidatePlans.get(0));
        }

        return candidatePlans.stream()
                .filter(plan -> coverageClassRepo.existsByPlan_IdAndCoverageClassTypeAndCoverageClassValueIgnoreCase(
                        plan.getId(),
                        CoverageClassType.PLAN,
                        cleanPolicyClassName
                ))
                .findFirst();
    }

    private CoverageType parseCoverageType(String value) {
        if (isBlank(value)) {
            throw new BadRequestAlertException(
                    "coverageTypeRequired",
                    "payorPlan",
                    "Coverage type is required."
            );
        }

        try {
            return CoverageType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestAlertException(
                    "invalidCoverageType",
                    "payorPlan",
                    "Invalid coverage type: " + value
            );
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String text = value.trim();

        return text.isEmpty() ? null : text;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
    // ---------------- HELPERS ----------------

    private void applyItemReference(
            PayorPlanItem item,
            BillingItemTypes itemType,
            Long brandMedicationId,
            Long diagnosticTestId,
            Long serviceId,
            Long procedureId
    ) {

        item.setBrandMedication(null);
        item.setDiagnosticTest(null);
        item.setService(null);
        item.setProcedure(null);

        switch (itemType) {

            case MEDICATION -> {

                if (brandMedicationId == null) {
                    throw new BadRequestAlertException(
                            "brandMedicationRequired",
                            "payorPlanItem",
                            "Brand medication is required."
                    );
                }

                BrandMedication brandMedication =
                        brandMedicationRepo.findById(brandMedicationId)
                                .orElseThrow(() -> new BadRequestAlertException(
                                        "brandMedicationNotFound",
                                        "payorPlanItem",
                                        "Brand medication not found."
                                ));

                item.setBrandMedication(brandMedication);
            }

            case LABORATORY, RADIOLOGY, PATHOLOGY -> {

                if (diagnosticTestId == null) {
                    throw new BadRequestAlertException(
                            "diagnosticTestRequired",
                            "payorPlanItem",
                            "Diagnostic test is required."
                    );
                }

                DiagnosticTest diagnosticTest =
                        diagnosticTestRepo.findById(diagnosticTestId)
                                .orElseThrow(() -> new BadRequestAlertException(
                                        "diagnosticTestNotFound",
                                        "payorPlanItem",
                                        "Diagnostic test not found."
                                ));

                item.setDiagnosticTest(diagnosticTest);
            }

            case SERVICE -> {

                if (serviceId == null) {
                    throw new BadRequestAlertException(
                            "serviceRequired",
                            "payorPlanItem",
                            "Service is required."
                    );
                }

                ServiceSetup service =
                        serviceRepo.findById(serviceId)
                                .orElseThrow(() -> new BadRequestAlertException(
                                        "serviceNotFound",
                                        "payorPlanItem",
                                        "Service not found."
                                ));

                item.setService(service);
            }

            case PROCEDURE -> {

                if (procedureId == null) {
                    throw new BadRequestAlertException(
                            "procedureRequired",
                            "payorPlanItem",
                            "Procedure is required."
                    );
                }

                Procedure procedure =
                        procedureRepo.findById(procedureId)
                                .orElseThrow(() -> new BadRequestAlertException(
                                        "procedureNotFound",
                                        "payorPlanItem",
                                        "Procedure not found."
                                ));

                item.setProcedure(procedure);
            }

            default -> throw new BadRequestAlertException(
                    "unsupportedItemType",
                    "payorPlanItem",
                    "Unsupported item type."
            );
        }
    }
}
