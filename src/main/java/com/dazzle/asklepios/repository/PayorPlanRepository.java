package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PayorPlan;
import com.dazzle.asklepios.domain.enumeration.CoverageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayorPlanRepository extends JpaRepository<PayorPlan, Long> {

    Page<PayorPlan> findByPayorId(Long payorId, Pageable pageable);

    Page<PayorPlan> findByIsActiveTrue(Pageable pageable);

    Page<PayorPlan> findByPayorIdAndIsActiveTrue(Long payorId, Pageable pageable);

    Optional<PayorPlan> findFirstByPayorIdAndWaseelPlanIdAndIsActiveTrue(
            Long payorId,
            String waseelPlanId
    );

    Optional<PayorPlan> findFirstByPayorIdAndCoverageTypeAndNetworkIdAndIsActiveTrue(
            Long payorId,
            CoverageType coverageType,
            String networkId
    );

    List<PayorPlan> findByPayorIdAndCoverageTypeAndNetworkIdAndIsActiveTrue(
            Long payorId,
            CoverageType coverageType,
            String networkId
    );

    List<PayorPlan> findByPayorIdAndCoverageTypeAndIsActiveTrue(
            Long payorId,
            CoverageType coverageType
    );
}