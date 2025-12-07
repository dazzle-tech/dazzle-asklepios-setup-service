package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTestReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnosticTestReportTemplateRepository extends JpaRepository<DiagnosticTestReportTemplate, Long> {

    Optional<DiagnosticTestReportTemplate> findByDiagnosticTest_Id(Long testId);

    boolean existsByDiagnosticTest_Id(Long testId);
}
