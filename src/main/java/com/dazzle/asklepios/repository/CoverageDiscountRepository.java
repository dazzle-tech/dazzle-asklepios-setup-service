package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageDiscount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoverageDiscountRepository extends JpaRepository<CoverageDiscount, Long> {

    Page<CoverageDiscount> findByCoverageContract_IdAndIsActive(
            Long contractId,
            Boolean isActive,
            Pageable pageable
    );

    Page<CoverageDiscount> findByCoverageContract_Id(Long contractId, Pageable pageable);
}
