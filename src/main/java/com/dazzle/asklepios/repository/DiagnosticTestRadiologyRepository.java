package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTestRadiology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnosticTestRadiologyRepository extends JpaRepository<DiagnosticTestRadiology, Long> {
    Optional<DiagnosticTestRadiology> findByTestId(Long testId);
    boolean existsByTestId(Long testId);
}
