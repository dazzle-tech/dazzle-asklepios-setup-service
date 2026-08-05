package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PayorPlan;
import com.dazzle.asklepios.domain.PayorPlanCoverageClass;
import com.dazzle.asklepios.repository.PayorPlanCoverageClassRepository;
import com.dazzle.asklepios.repository.PayorPlanRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.payorplan.PayorPlanCoverageClassSaveVM;
import com.dazzle.asklepios.web.rest.vm.payorplan.PayorPlanCoverageClassUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PayorPlanCoverageClassService {

    private static final Logger LOG = LoggerFactory.getLogger(PayorPlanCoverageClassService.class);

    private final PayorPlanCoverageClassRepository coverageClassRepo;
    private final PayorPlanRepository planRepo;

    public PayorPlanCoverageClassService(
            PayorPlanCoverageClassRepository coverageClassRepo,
            PayorPlanRepository planRepo
    ) {
        this.coverageClassRepo = coverageClassRepo;
        this.planRepo = planRepo;
    }

    public PayorPlanCoverageClass create(PayorPlanCoverageClassSaveVM vm) {
        LOG.debug("Create PayorPlanCoverageClass payload={}", vm);

        PayorPlan plan = planRepo.findById(vm.planId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "planNotFound",
                        "payorPlanCoverageClass",
                        "Plan not found."
                ));

        if (coverageClassRepo.existsByPlan_IdAndCoverageClassTypeAndCoverageClassValueIgnoreCase(
                vm.planId(),
                vm.coverageClassType(),
                vm.coverageClassValue()
        )) {
            throw new BadRequestAlertException(
                    "coverageClassExists",
                    "payorPlanCoverageClass",
                    "Coverage class already exists for this plan."
            );
        }

        PayorPlanCoverageClass coverageClass = PayorPlanCoverageClass.builder()
                .plan(plan)
                .coverageClassType(vm.coverageClassType())
                .coverageClassValue(vm.coverageClassValue())
                .coverageClassName(vm.coverageClassName())
                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .build();

        return coverageClassRepo.save(coverageClass);
    }

    public PayorPlanCoverageClass update(PayorPlanCoverageClassUpdateVM vm) {
        LOG.debug("Update PayorPlanCoverageClass payload={}", vm);

        PayorPlanCoverageClass existing = coverageClassRepo.findById(vm.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound",
                        "payorPlanCoverageClass",
                        "Coverage class not found."
                ));

        PayorPlan plan = planRepo.findById(vm.planId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "planNotFound",
                        "payorPlanCoverageClass",
                        "Plan not found."
                ));

        boolean duplicated = coverageClassRepo.existsByPlan_IdAndCoverageClassTypeAndCoverageClassValueIgnoreCase(
                vm.planId(),
                vm.coverageClassType(),
                vm.coverageClassValue()
        );

        boolean sameBusinessKey =
                existing.getPlan().getId().equals(vm.planId())
                        && existing.getCoverageClassType() == vm.coverageClassType()
                        && existing.getCoverageClassValue().equalsIgnoreCase(vm.coverageClassValue());

        if (duplicated && !sameBusinessKey) {
            throw new BadRequestAlertException(
                    "coverageClassExists",
                    "payorPlanCoverageClass",
                    "Coverage class already exists for this plan."
            );
        }

        existing.setPlan(plan);
        existing.setCoverageClassType(vm.coverageClassType());
        existing.setCoverageClassValue(vm.coverageClassValue());
        existing.setCoverageClassName(vm.coverageClassName());
        existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());

        return coverageClassRepo.save(existing);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<PayorPlanCoverageClass> findOne(Long id) {
        return coverageClassRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<PayorPlanCoverageClass> getByPlan(Long planId, Pageable pageable) {
        return coverageClassRepo.findByPlan_Id(planId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PayorPlanCoverageClass> getActiveByPlan(Long planId, Pageable pageable) {
        return coverageClassRepo.findByPlan_IdAndIsActiveTrue(planId, pageable);
    }

    public java.util.Optional<PayorPlanCoverageClass> toggleIsActive(Long id) {
        return coverageClassRepo.findById(id)
                .map(c -> {
                    c.setIsActive(!Boolean.TRUE.equals(c.getIsActive()));
                    return coverageClassRepo.save(c);
                });
    }

    public void delete(Long id) {
        coverageClassRepo.deleteById(id);
    }

    public void deleteByPlanId(Long planId) {
        coverageClassRepo.deleteByPlan_Id(planId);
    }
}