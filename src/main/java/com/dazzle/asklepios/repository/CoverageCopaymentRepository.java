package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageCopayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoverageCopaymentRepository extends JpaRepository<CoverageCopayment, Long> {

    Page<CoverageCopayment> findByCoverageContract_IdAndIsActive(
            Long contractId,
            Boolean isActive,
            Pageable pageable
    );

    Page<CoverageCopayment> findByCoverageContract_Id(Long contractId, Pageable pageable);

    List<CoverageCopayment> findByCoverageContract_IdAndIsActiveTrueOrderByLastModifiedDateDesc(Long contractId);
}
