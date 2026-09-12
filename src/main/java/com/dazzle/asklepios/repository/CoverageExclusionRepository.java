package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageExclusion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoverageExclusionRepository extends JpaRepository<CoverageExclusion, Long> {

    Page<CoverageExclusion> findByCoverageContract_IdAndIsActive(
            Long contractId,
            Boolean isActive,
            Pageable pageable
    );

    Page<CoverageExclusion> findByCoverageContract_Id(Long contractId, Pageable pageable);

    Page<CoverageExclusion> findByTpaDefinition_IdAndIsActive(
            Long tpaId,
            Boolean isActive,
            Pageable pageable
    );

    Page<CoverageExclusion> findByTpaDefinition_Id(Long tpaId, Pageable pageable);
}
