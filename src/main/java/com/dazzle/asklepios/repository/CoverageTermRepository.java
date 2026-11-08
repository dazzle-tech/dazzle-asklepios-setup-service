package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageTerm;
import com.dazzle.asklepios.domain.enumeration.CoverageTermType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoverageTermRepository extends JpaRepository<CoverageTerm, Long> {

    Page<CoverageTerm> findByCoverageClass_IdAndTermTypeAndIsActive(
            Long classId,
            CoverageTermType termType,
            Boolean isActive,
            Pageable pageable
    );

    Page<CoverageTerm> findByCoverageClass_IdAndTermType(
            Long classId,
            CoverageTermType termType,
            Pageable pageable
    );

    List<CoverageTerm> findByCoverageClass_IdAndTermTypeAndIsActiveTrue(
            Long classId,
            CoverageTermType termType
    );
}
