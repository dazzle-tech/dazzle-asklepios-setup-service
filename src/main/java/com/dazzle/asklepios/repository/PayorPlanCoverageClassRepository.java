package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PayorPlanCoverageClass;
import com.dazzle.asklepios.domain.enumeration.CoverageClassType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayorPlanCoverageClassRepository extends JpaRepository<PayorPlanCoverageClass, Long> {

    Page<PayorPlanCoverageClass> findByPlan_Id(Long planId, Pageable pageable);

    Page<PayorPlanCoverageClass> findByPlan_IdAndIsActiveTrue(Long planId, Pageable pageable);

    boolean existsByPlan_IdAndCoverageClassTypeAndCoverageClassValueIgnoreCase(
            Long planId,
            CoverageClassType coverageClassType,
            String coverageClassValue
    );

    void deleteByPlan_Id(Long planId);
}