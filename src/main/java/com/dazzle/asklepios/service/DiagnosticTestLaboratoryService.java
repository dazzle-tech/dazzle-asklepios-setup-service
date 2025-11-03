package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTestLaboratory;
import com.dazzle.asklepios.repository.DiagnosticTestLaboratoryRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class DiagnosticTestLaboratoryService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestLaboratoryService.class);

    private final DiagnosticTestLaboratoryRepository repository;

    public DiagnosticTestLaboratoryService(DiagnosticTestLaboratoryRepository repository) {
        this.repository = repository;
    }

    /**
     * Create a new DiagnosticTestLaboratory record.
     * Validates that no other laboratory record exists for the same test.
     */
    public DiagnosticTestLaboratory create(DiagnosticTestLaboratory entity) {
        LOG.debug("Request to create DiagnosticTestLaboratory: {}", entity);

        if (entity.getTest() != null && entity.getTest().getId() != null) {
            Long testId = entity.getTest().getId();

            // Prevent duplicate records for the same test
            if (repository.existsByTestId(testId)) {
                LOG.warn("Attempt to create duplicate DiagnosticTestLaboratory for testId={}", testId);
                throw new BadRequestAlertException(
                        "Laboratory already exists for test id " + testId,
                        "diagnosticTestLaboratory",
                        "duplicate"
                );
            }
        } else {
            LOG.error("Invalid create request - testId is missing");
            throw new BadRequestAlertException(
                    "testId is required when creating a DiagnosticTestLaboratory",
                    "diagnosticTestLaboratory",
                    "missingtestid"
            );
        }

        DiagnosticTestLaboratory saved = repository.save(entity);
        LOG.info("Created DiagnosticTestLaboratory with id={} for testId={}", saved.getId(), entity.getTest().getId());
        return saved;
    }

    /**
     * Update an existing DiagnosticTestLaboratory.
     */
    public Optional<DiagnosticTestLaboratory> update(Long id, DiagnosticTestLaboratory entity) {
        LOG.debug("Request to update DiagnosticTestLaboratory id={} with new data", id);

        return repository.findById(id).map(existing -> {
            entity.setId(existing.getId());
            DiagnosticTestLaboratory updated = repository.save(entity);
            LOG.info("Updated DiagnosticTestLaboratory with id={}", updated.getId());
            return updated;
        });
    }

    /**
     * Retrieve all DiagnosticTestLaboratory records (paginated).
     */
    @Transactional(readOnly = true)
    public Page<DiagnosticTestLaboratory> findAll(Pageable pageable) {
        LOG.debug("Request to get all DiagnosticTestLaboratories (page={}, size={})",
                pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable);
    }

    /**
     * Find one DiagnosticTestLaboratory by its ID.
     */
    @Transactional(readOnly = true)
    public Optional<DiagnosticTestLaboratory> findOne(Long id) {
        LOG.debug("Request to get DiagnosticTestLaboratory by id={}", id);
        return repository.findById(id);
    }

    /**
     * Delete a DiagnosticTestLaboratory by ID.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete DiagnosticTestLaboratory id={}", id);

        if (!repository.existsById(id)) {
            LOG.warn("Attempt to delete non-existing DiagnosticTestLaboratory id={}", id);
            throw new BadRequestAlertException("DiagnosticTestLaboratory not found", "diagnosticTestLaboratory", "notfound");
        }
        repository.deleteById(id);
        LOG.info("Deleted DiagnosticTestLaboratory id={}", id);
    }

    /**
     * Retrieve a DiagnosticTestLaboratory record by its associated test ID.
     */
    @Transactional(readOnly = true)
    public DiagnosticTestLaboratory getByTestId(Long testId) {
        LOG.debug("Request to get DiagnosticTestLaboratory by testId={}", testId);

        return repository.findByTestId(testId)
                .orElseThrow(() -> {
                    LOG.warn("No DiagnosticTestLaboratory found for testId={}", testId);
                    return new BadRequestAlertException(
                            "DiagnosticTestLaboratory not found for test id " + testId,
                            "diagnosticTestLaboratory",
                            "notfound"
                    );
                });
    }
}
