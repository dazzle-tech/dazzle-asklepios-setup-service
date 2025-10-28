package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.enumeration.TestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosticTestRepository extends JpaRepository<DiagnosticTest, Long> {

    Page<DiagnosticTest> findByTestType(TestType type, Pageable pageable);

    Page<DiagnosticTest> findByTestNameContainingIgnoreCase(String name, Pageable pageable);
}
