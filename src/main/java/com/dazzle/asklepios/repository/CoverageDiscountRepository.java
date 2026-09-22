package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageDiscount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoverageDiscountRepository extends JpaRepository<CoverageDiscount, Long> {

    Page<CoverageDiscount> findByCoverageClass_IdAndIsActive(Long classId, Boolean isActive, Pageable pageable);

    Page<CoverageDiscount> findByCoverageClass_Id(Long classId, Pageable pageable);

    Page<CoverageDiscount> findByTpaDefinition_IdAndIsActive(Long tpaId, Boolean isActive, Pageable pageable);

    Page<CoverageDiscount> findByTpaDefinition_Id(Long tpaId, Pageable pageable);

    List<CoverageDiscount> findByCoverageClass_IdAndIsActiveTrue(Long classId);

    List<CoverageDiscount> findByTpaDefinition_IdAndIsActiveTrue(Long tpaId);
}
