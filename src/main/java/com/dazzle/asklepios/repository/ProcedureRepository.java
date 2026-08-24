package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.enumeration.ProcedureCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcedureRepository extends JpaRepository<Procedure, Long> {

    Page<Procedure> findByFacility_Id(Long facilityId, Pageable pageable);
    Page<Procedure> findByFacility_IdAndCategoryType(Long facilityId, ProcedureCategory category, Pageable pageable);

    Page<Procedure> findByCategoryType(ProcedureCategory categoryType, Pageable pageable);

    Page<Procedure> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Procedure> findByCodeContainingIgnoreCase(String code, Pageable pageable);
    Page<Procedure> findByIsActiveTrueAndIsAppointableTrue(

            Pageable pageable
    );
    Page<Procedure> findByFacility_IdAndIsActiveTrue(Long facilityId, Pageable pageable);

    Page<Procedure> findByFacility_IdAndIsActiveTrueAndCategoryType(
            Long facilityId, ProcedureCategory categoryType, Pageable pageable);

    Page<Procedure> findByFacility_IdAndIsActiveTrueAndCategoryTypeAndNameContainingIgnoreCase(
            Long facilityId,
            ProcedureCategory categoryType,
            String name,
            Pageable pageable);
}
