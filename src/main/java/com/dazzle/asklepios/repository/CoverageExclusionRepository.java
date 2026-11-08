package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageExclusion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoverageExclusionRepository extends JpaRepository<CoverageExclusion, Long> {

    Page<CoverageExclusion> findByCoverageClass_IdAndIsActive(Long classId, Boolean isActive, Pageable pageable);

    Page<CoverageExclusion> findByCoverageClass_Id(Long classId, Pageable pageable);

    Page<CoverageExclusion> findByTpaDefinition_IdAndIsActive(Long tpaId, Boolean isActive, Pageable pageable);

    Page<CoverageExclusion> findByTpaDefinition_Id(Long tpaId, Pageable pageable);

    List<CoverageExclusion> findByCoverageClass_IdAndIsActiveTrue(Long classId);

    List<CoverageExclusion> findByTpaDefinition_IdAndIsActiveTrue(Long tpaId);
}
