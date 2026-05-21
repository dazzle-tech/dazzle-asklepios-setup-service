package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PayorPlan;
import com.dazzle.asklepios.domain.PayorPlanItem;
import com.dazzle.asklepios.repository.PayorPlanItemRepository;
import com.dazzle.asklepios.repository.PayorPlanRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.payorplan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PayorPlanService {

    private static final Logger LOG = LoggerFactory.getLogger(PayorPlanService.class);

    private final PayorPlanRepository planRepo;
    private final PayorPlanItemRepository itemRepo;

    public PayorPlanService(PayorPlanRepository planRepo, PayorPlanItemRepository itemRepo) {
        this.planRepo = planRepo;
        this.itemRepo = itemRepo;
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
                        "notFound", "payorPlan", "Plan not found."
                ));

        existing.setPayorId(vm.payorId());
        existing.setName(vm.name());
        existing.setPlanType(vm.planType());
        existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());
        existing.setPayorId(vm.payorId());
        existing.setName(vm.name());
        existing.setPlanType(vm.planType());

        existing.setNetworkId(vm.networkId());
        existing.setCoverageType(vm.coverageType());
        existing.setPayerNphiesId(vm.payerNphiesId());
        existing.setWaseelPlanId(vm.waseelPlanId());

        existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());
        return planRepo.save(existing);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<PayorPlan> findOnePlan(Long id) {
        return planRepo.findById(id);
    }

    public void deletePlan(Long id) {
        // delete items first (orphanRemoval already, but safe)
        itemRepo.deleteByPlan_Id(id);
        planRepo.deleteById(id);
    }

    public java.util.Optional<PayorPlan> togglePlanActive(Long id) {
        return planRepo.findById(id)
                .map(p -> {
                    p.setIsActive(!Boolean.TRUE.equals(p.getIsActive()));
                    return planRepo.save(p);
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
                        "planNotFound", "payorPlanItem", "Plan not found."
                ));

        PayorPlanItem i = PayorPlanItem.builder()
                .plan(plan)
                .itemType(vm.itemType())
                .amount(vm.amount())
                .coverageType(vm.coverageType())
                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .build();

        return itemRepo.save(i);
    }

    public PayorPlanItem updateItem(PayorPlanItemUpdateVM vm) {
        PayorPlanItem existing = itemRepo.findById(vm.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound", "payorPlanItem", "Plan item not found."
                ));

        PayorPlan plan = planRepo.findById(vm.planId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "planNotFound", "payorPlanItem", "Plan not found."
                ));

        existing.setPlan(plan);
        existing.setItemType(vm.itemType());
        existing.setAmount(vm.amount());
        existing.setCoverageType(vm.coverageType());
        existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());

        return itemRepo.save(existing);
    }

    public void deleteItem(Long id) {
        itemRepo.deleteById(id);
    }

    public java.util.Optional<PayorPlanItem> toggleItemActive(Long id) {
        return itemRepo.findById(id)
                .map(i -> {
                    i.setIsActive(!Boolean.TRUE.equals(i.getIsActive()));
                    return itemRepo.save(i);
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

        return planRepo.findFirstByPayorIdAndCoverageTypeAndNetworkIdAndPolicyClassNameAndIsActiveTrue(
                payorId,
                cleanCoverageType,
                cleanNetworkId,
                cleanPolicyClassName
        );
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
