package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageTermItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoverageTermItemRepository extends JpaRepository<CoverageTermItem, Long> {

    Page<CoverageTermItem> findByCoverageTerm_IdAndIsActive(
            Long termId,
            Boolean isActive,
            Pageable pageable
    );

    Page<CoverageTermItem> findByCoverageTerm_Id(Long termId, Pageable pageable);
}
