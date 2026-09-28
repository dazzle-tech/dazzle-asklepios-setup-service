package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTestNormalRange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiagnosticTestNormalRangeRepository extends JpaRepository<DiagnosticTestNormalRange, Long> {
    Page<DiagnosticTestNormalRange> findByTest_Id(Long testId, Pageable pageable);
    Page<DiagnosticTestNormalRange> findByProfileTest_Id(Long profileTestId, Pageable pageable);
    List<DiagnosticTestNormalRange> findAllByProfileTest_Id(Long profileTestId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update DiagnosticTestNormalRange r set r.isActive = false where r.profileTest.id = :profileTestId and r.isActive = true")
    int deactivateActiveByProfileTestId(@Param("profileTestId") Long profileTestId);
}
