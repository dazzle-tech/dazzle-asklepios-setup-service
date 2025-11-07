package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTestNormalRangeLov;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosticTestNormalRangeLovRepository extends JpaRepository<DiagnosticTestNormalRangeLov, Long> {
    @Transactional
    void deleteByNormalRangeId(Long normalRangeId);
    List<DiagnosticTestNormalRangeLov> findByNormalRangeId(Long normalRangeId);
}
