package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PayorPlanItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayorPlanItemRepository extends JpaRepository<PayorPlanItem, Long> {

    Page<PayorPlanItem> findByPlan_Id(Long planId, Pageable pageable);

    Page<PayorPlanItem> findByPlan_IdAndIsActiveTrue(Long planId, Pageable pageable);

    void deleteByPlan_Id(Long planId);
}
