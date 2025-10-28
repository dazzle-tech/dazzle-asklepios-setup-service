package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.enumeration.ProcedureCategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedureRepository extends JpaRepository<Procedure, Long> {

    List<Procedure> findByFacility_Id(Long facilityId);

    Page<Procedure> findByFacility_Id(Long facilityId, Pageable pageable);

    Page<Procedure> findByFacility_IdAndCategoryType(Long facilityId, ProcedureCategoryType categoryType, Pageable pageable);

    Page<Procedure> findByFacility_IdAndNameContainingIgnoreCase(Long facilityId, String name, Pageable pageable);

    Page<Procedure> findByFacility_IdAndCodeContainingIgnoreCase(Long facilityId, String code, Pageable pageable);

}
