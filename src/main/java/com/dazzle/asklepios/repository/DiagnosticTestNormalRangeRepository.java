package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTestNormalRange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosticTestNormalRangeRepository extends JpaRepository<DiagnosticTestNormalRange, Long> {
    Page<DiagnosticTestNormalRange> findByTest_Id(Long testId, Pageable pageable);
    Page<DiagnosticTestNormalRange> findByProfileTest_Id(Long profileTestId, Pageable pageable);
    List<DiagnosticTestNormalRange> findAllByProfileTest_Id(Long profileTestId);
}
