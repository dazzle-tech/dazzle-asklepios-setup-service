package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.FavoriteDiagnosticTest;
import com.dazzle.asklepios.repository.FavoriteDiagnosticTestRepository;
import com.dazzle.asklepios.service.dto.FavoriteDiagnosticsTests.FavoriteDiagnosticTestCreateDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FavoriteDiagnosticTestService {

    private static final Logger LOG = LoggerFactory.getLogger(FavoriteDiagnosticTestService.class);

    private final FavoriteDiagnosticTestRepository repository;

    public FavoriteDiagnosticTestService(FavoriteDiagnosticTestRepository repository) {
        this.repository = repository;
    }


    public FavoriteDiagnosticTest add(FavoriteDiagnosticTestCreateDTO dto) {
        LOG.debug("Request to add FavoriteDiagnosticTest: userId={}, testId={}", dto.userId(), dto.testId());

        boolean exists = repository.existsByUserIdAndTestId(dto.userId(), dto.testId());
        if (exists) {
            LOG.warn("FavoriteDiagnosticTest already exists: userId={}, testId={}", dto.userId(), dto.testId());
            throw  new BadRequestAlertException(
                    "favorites_found",
                    "favoritesDiagnosticTest",
                    "Diagnostic test already in favorites"
            );
        }

        FavoriteDiagnosticTest entity = FavoriteDiagnosticTest.builder()
                .userId(dto.userId())
                .testId(dto.testId())
                .build();

        FavoriteDiagnosticTest saved = repository.save(entity);
        LOG.info("FavoriteDiagnosticTest added successfully: id={}, userId={}, testId={}",
                saved.getId(), saved.getUserId(), saved.getTestId());

        return saved;
    }


    public void delete(Long userId, Long testId) {
        LOG.debug("Request to delete FavoriteDiagnosticTest: userId={}, testId={}", userId, testId);

        FavoriteDiagnosticTest entity = repository.findByUserIdAndTestId(userId, testId)
                .orElseThrow(() -> {
                    LOG.warn("FavoriteDiagnosticTest not found for delete: userId={}, testId={}", userId, testId);
                    return new NotFoundAlertException(
                            "Favorite diagnostic test not found",
                            "favoriteDiagnosticTest",
                            "notfound"
                    );
                });


        repository.delete(entity);
        LOG.info("FavoriteDiagnosticTest deleted successfully: id={}, userId={}, testId={}",
                entity.getId(), entity.getUserId(), entity.getTestId());
    }


    @Transactional(readOnly = true)
    public Page<FavoriteDiagnosticTest> findFavoriteDiagnosticsTestsByUserId(Long userId, Pageable pageable) {
        LOG.debug("Request to get FavoriteDiagnosticTests by userId={}, page={}, size={}, sort={}",
                userId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<FavoriteDiagnosticTest> page = repository.findAllByUserId(userId, pageable);

        LOG.debug("FavoriteDiagnosticTests fetched: userId={}, returnedElements={}, totalElements={}, totalPages={}",
                userId, page.getNumberOfElements(), page.getTotalElements(), page.getTotalPages());

        return page;
    }
}
