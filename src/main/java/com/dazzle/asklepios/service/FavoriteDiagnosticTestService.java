package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.FavoriteDiagnosticTest;
import com.dazzle.asklepios.repository.FavoriteDiagnosticTestRepository;
import com.dazzle.asklepios.service.dto.FavoriteDiagnosticsTests.FavoriteDiagnosticTestCreateDTO;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteDiagnosticTestService {

    private final FavoriteDiagnosticTestRepository repository;

    /* ========================= ADD ========================= */

    public FavoriteDiagnosticTest add(FavoriteDiagnosticTestCreateDTO dto) {

        if (repository.existsByUserIdAndTestId(dto.userId(), dto.testId())) {
            throw new EntityExistsException("Diagnostic test already in favorites");
        }

        FavoriteDiagnosticTest entity = FavoriteDiagnosticTest.builder()
                .userId(dto.userId())
                .testId(dto.testId())
                .build();

        return repository.save(entity);
    }

    /* ========================= DELETE ========================= */

    public void delete(Long userId, Long testId) {

        FavoriteDiagnosticTest entity = repository
                .findByUserIdAndTestId(userId, testId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Favorite diagnostic test not found")
                );

        repository.delete(entity);
    }

    public Page<FavoriteDiagnosticTest> findFavoriteDiagnosticsTestsByUserId(
            Long userId,
            Pageable pageable
    ) {
        return repository.findAllByUserId(userId, pageable);
    }
}
