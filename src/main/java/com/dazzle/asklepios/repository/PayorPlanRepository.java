package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PayorPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayorPlanRepository extends JpaRepository<PayorPlan, Long> {
    Page<PayorPlan> findByPayorId(Long payorId, Pageable pageable);
}

