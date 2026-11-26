package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageTermExclusion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoverageTermExclusionRepository extends JpaRepository<CoverageTermExclusion, Long> {

    Page<CoverageTermExclusion> findByCoverageTerm_IdAndIsActive(
            Long termId,
            Boolean isActive,
            Pageable pageable
    );

    Page<CoverageTermExclusion> findByCoverageTerm_Id(Long termId, Pageable pageable);

    List<CoverageTermExclusion> findByCoverageTerm_IdAndIsActiveTrue(Long termId);
}
