package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageCopayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoverageCopaymentRepository extends JpaRepository<CoverageCopayment, Long> {

    Page<CoverageCopayment> findByCoverageClass_IdAndIsActive(Long classId, Boolean isActive, Pageable pageable);

    Page<CoverageCopayment> findByCoverageClass_Id(Long classId, Pageable pageable);

    List<CoverageCopayment> findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(Long classId);
}
