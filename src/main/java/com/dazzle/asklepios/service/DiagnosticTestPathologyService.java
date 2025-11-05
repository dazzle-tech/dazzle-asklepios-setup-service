package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTestPathology;
import com.dazzle.asklepios.repository.DiagnosticTestPathologyRepository;
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
public class DiagnosticTestPathologyService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestPathologyService.class);

    private final DiagnosticTestPathologyRepository repository;

    public DiagnosticTestPathologyService(DiagnosticTestPathologyRepository repository) {
        this.repository = repository;
    }

    public DiagnosticTestPathology create(DiagnosticTestPathology entity) {
        if (repository.existsByTest_Id(entity.getTest().getId())) {
            throw new BadRequestAlertException("Pathology already exists for this test", "diagnosticTestPathology", "duplicate");
        }
        LOG.info("Creating new DiagnosticTestPathology for testId={}", entity.getTest().getId());
        return repository.save(entity);
    }

    public Optional<DiagnosticTestPathology> update(Long id, DiagnosticTestPathology entity) {
        return repository.findById(id).map(existing -> {
            entity.setId(existing.getId());
            LOG.info("Updating DiagnosticTestPathology id={}", id);
            return repository.save(entity);
        });
    }

    @Transactional(readOnly = true)
    public Page<DiagnosticTestPathology> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<DiagnosticTestPathology> findOne(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<DiagnosticTestPathology> findByTestId(Long testId) {
        return repository.findByTest_Id(testId);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BadRequestAlertException("DiagnosticTestPathology not found", "diagnosticTestPathology", "notfound");
        }
        LOG.info("Deleting DiagnosticTestPathology id={}", id);
        repository.deleteById(id);
    }
}
