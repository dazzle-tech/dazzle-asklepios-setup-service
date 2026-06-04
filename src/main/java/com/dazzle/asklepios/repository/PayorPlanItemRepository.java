package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PayorPlanItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayorPlanItemRepository extends JpaRepository<PayorPlanItem, Long> {

    Page<PayorPlanItem> findByPlan_Id(Long planId, Pageable pageable);

    Page<PayorPlanItem> findByPlan_IdAndIsActiveTrue(Long planId, Pageable pageable);

    boolean existsByProcedure_IdAndPreAuthorizationTrueAndIsActiveTrue(Long procedureId);

    boolean existsByService_IdAndPreAuthorizationTrueAndIsActiveTrue(Long serviceId);

    boolean existsByDiagnosticTest_IdAndPreAuthorizationTrueAndIsActiveTrue(Long diagnosticTestId);

    boolean existsByBrandMedication_IdAndPreAuthorizationTrueAndIsActiveTrue(Long brandMedicationId);
    void deleteByPlan_Id(Long planId);
}