package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Payor;
import com.dazzle.asklepios.domain.PayorPlan;
import com.dazzle.asklepios.repository.PayorPlanRepository;
import com.dazzle.asklepios.repository.PayorRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.payorPlan.PayorPlanSaveVM;
import com.dazzle.asklepios.web.rest.vm.payorPlan.PayorPlanUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PayorPlanService {

    private static final Logger LOG = LoggerFactory.getLogger(PayorPlanService.class);

    private final PayorPlanRepository planRepo;
    private final PayorRepository payorRepo;

    public PayorPlanService(PayorPlanRepository planRepo, PayorRepository payorRepo) {
        this.planRepo = planRepo;
        this.payorRepo = payorRepo;
    }

    public PayorPlan create(PayorPlanSaveVM vm) {
        LOG.debug("Create PayorPlan payload={}", vm);

        Payor payor = payorRepo.findById(vm.payorId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "payorNotFound", "payorPlan", "Payor not found."
                ));

        validateMandatory(vm);

        PayorPlan plan = PayorPlan.builder()
                .payor(payor)
                .name(vm.name())
                .planType(vm.planType())
                .itemType(vm.itemType())
                .amount(vm.amount())
                .coverageType(vm.coverageType())
                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .build();

        return planRepo.save(plan);
    }

    public PayorPlan update(PayorPlanUpdateVM vm) {
        LOG.debug("Update PayorPlan payload={}", vm);

        PayorPlan existing = planRepo.findById(vm.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound", "payorPlan", "Plan not found."
                ));

        Payor payor = payorRepo.findById(vm.payorId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "payorNotFound", "payorPlan", "Payor not found."
                ));

        validateMandatory(vm);

        existing.setPayor(payor);
        existing.setName(vm.name());
        existing.setPlanType(vm.planType());
        existing.setItemType(vm.itemType());
        existing.setAmount(vm.amount());
        existing.setCoverageType(vm.coverageType());
        existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());

        return planRepo.save(existing);
    }

    private void validateMandatory(PayorPlanSaveVM vm) {
        if (vm.name() == null || vm.name().isBlank()
                || vm.planType() == null
                || vm.itemType() == null
                || vm.coverageType() == null) {
            throw new BadRequestAlertException(
                    "mandatoryMissing", "payorPlan", "Mandatory fields are missing."
            );
        }
    }

    private void validateMandatory(PayorPlanUpdateVM vm) {
        if (vm.name() == null || vm.name().isBlank()
                || vm.planType() == null
                || vm.itemType() == null
                || vm.coverageType() == null) {
            throw new BadRequestAlertException(
                    "mandatoryMissing", "payorPlan", "Mandatory fields are missing."
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<PayorPlan> getByPayor(Long payorId, Pageable pageable) {
        return planRepo.findByPayorId(payorId, pageable);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<PayorPlan> findOne(Long id) {
        return planRepo.findById(id);
    }

    public void delete(Long id) {
        if (!planRepo.existsById(id)) {
            throw new BadRequestAlertException(
                    "notFound", "payorPlan", "Plan not found."
            );
        }
        planRepo.deleteById(id);
    }
}

