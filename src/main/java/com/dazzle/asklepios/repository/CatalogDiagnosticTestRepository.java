package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CatalogDiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogDiagnosticTestRepository extends JpaRepository<CatalogDiagnosticTest, Long> {
    Page<CatalogDiagnosticTest> findByCatalog_Id(Long catalogId, Pageable pageable);
    boolean existsByCatalog_IdAndTest_Id(Long catalogId, Long testId);
    void deleteByCatalog_IdAndTest_Id(Long catalogId, Long testId);
    void deleteByCatalog_Id(Long catalogId);
    long countByTest(DiagnosticTest test);
}
