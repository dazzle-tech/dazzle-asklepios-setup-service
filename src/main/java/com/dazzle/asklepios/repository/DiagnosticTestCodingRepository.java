package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTestCoding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticTestCodingRepository extends JpaRepository<DiagnosticTestCoding, Long> {

    Page<DiagnosticTestCoding> findByDiagnosticTestId(Long diagnosticTestId, Pageable pageable);
}
