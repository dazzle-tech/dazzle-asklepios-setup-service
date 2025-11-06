package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTestRadiology;
import com.dazzle.asklepios.repository.DiagnosticTestRadiologyRepository;
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
public class DiagnosticTestRadiologyService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestRadiologyService.class);
    private final DiagnosticTestRadiologyRepository repository;

    public DiagnosticTestRadiologyService(DiagnosticTestRadiologyRepository repository) {
        this.repository = repository;
    }

    public DiagnosticTestRadiology create(DiagnosticTestRadiology entity) {
        LOG.debug("Creating DiagnosticTestRadiology for test_id={}", entity.getTest().getId());
        if (repository.existsByTestId(entity.getTest().getId())) {
            throw new BadRequestAlertException("Radiology already exists for this test_id", "diagnosticTestRadiology", "exists");
        }
        return repository.save(entity);
    }

    public Optional<DiagnosticTestRadiology> update(Long id, DiagnosticTestRadiology entity) {
        return repository.findById(id).map(existing -> {
            entity.setId(existing.getId());
            LOG.debug("Updating DiagnosticTestRadiology id={}", id);
            return repository.save(entity);
        });
    }

    @Transactional(readOnly = true)
    public Page<DiagnosticTestRadiology> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<DiagnosticTestRadiology> findOne(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<DiagnosticTestRadiology> findByTestId(Long testId) {
        return repository.findByTestId(testId);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BadRequestAlertException("DiagnosticTestRadiology not found", "diagnosticTestRadiology", "notfound");
        }
        repository.deleteById(id);
        LOG.info("Deleted DiagnosticTestRadiology id={}", id);
    }
}
