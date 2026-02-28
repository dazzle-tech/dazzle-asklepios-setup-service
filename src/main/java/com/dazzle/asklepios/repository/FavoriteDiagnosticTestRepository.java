package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.FavoriteDiagnosticTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteDiagnosticTestRepository
        extends JpaRepository<FavoriteDiagnosticTest, Long> {

    Optional<FavoriteDiagnosticTest> findByUserIdAndTestId(Long userId, Long testId);

    void deleteByUserIdAndTestId(Long userId, Long testId);

    boolean existsByUserIdAndTestId(Long userId, Long testId);

    Page<FavoriteDiagnosticTest> findAllByUserId(Long userId, Pageable pageable);

}
