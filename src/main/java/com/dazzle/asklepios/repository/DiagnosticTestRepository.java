package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.enumeration.TestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticTestRepository extends JpaRepository<DiagnosticTest, Long> {

    Page<DiagnosticTest> findByType(TestType type, Pageable pageable);
    List<DiagnosticTest> findAllByType(TestType type);
    Page<DiagnosticTest> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<DiagnosticTest> findByIsActiveTrue(Pageable pageable);

    Page<DiagnosticTest> findByTypeAndNameContainingIgnoreCase(TestType type, String name, Pageable pageable);
}
