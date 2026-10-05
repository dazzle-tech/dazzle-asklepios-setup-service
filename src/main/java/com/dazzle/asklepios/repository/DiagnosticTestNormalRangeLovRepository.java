package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTestNormalRangeLov;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiagnosticTestNormalRangeLovRepository extends JpaRepository<DiagnosticTestNormalRangeLov, Long> {
    @Transactional
    void deleteByNormalRangeId(Long normalRangeId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from DiagnosticTestNormalRangeLov lov where lov.normalRange.profileTest.id = :profileTestId")
    int deleteAllByProfileTestId(@Param("profileTestId") Long profileTestId);

    List<DiagnosticTestNormalRangeLov> findByNormalRangeId(Long normalRangeId);
}
