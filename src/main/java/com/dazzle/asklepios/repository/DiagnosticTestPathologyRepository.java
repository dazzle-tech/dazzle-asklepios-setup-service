package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTestPathology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnosticTestPathologyRepository extends JpaRepository<DiagnosticTestPathology, Long> {

    Optional<DiagnosticTestPathology> findByTest_Id(Long testId);

    boolean existsByTest_Id(Long testId);
}
