package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.repository.DiagnosticTestProfileRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DiagnosticTestProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestProfileService.class);
    private final DiagnosticTestProfileRepository repository;

    public DiagnosticTestProfileService(DiagnosticTestProfileRepository repository) {
        this.repository = repository;
    }

    public DiagnosticTestProfile create(DiagnosticTestProfile entity) {
        LOG.info("Creating DiagnosticTestProfile for testId={}",
                entity.getTest() != null ? entity.getTest().getId() : null);
        return repository.save(entity);
    }

    public Optional<DiagnosticTestProfile> update(Long id, DiagnosticTestProfile entity) {
        LOG.info("Updating DiagnosticTestProfile id={}", id);
        return repository.findById(id).map(existing -> {
            entity.setId(existing.getId());
            return repository.save(entity);
        });
    }

    @Transactional(readOnly = true)
    public Page<DiagnosticTestProfile> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<DiagnosticTestProfile> findAllByTestId(Long testId) {
        return repository.findAllByTest_Id(testId);
    }

    @Transactional(readOnly = true)
    public Optional<DiagnosticTestProfile> findOne(Long id) {
        return repository.findById(id);
    }

    public void delete(Long id) {
        LOG.info("Deleting DiagnosticTestProfile id={}", id);
        if (!repository.existsById(id)) {
            throw new BadRequestAlertException("Profile not found", "diagnosticTestProfile", "notfound");
        }
        repository.deleteById(id);
    }

    public void deleteAllByTestId(Long testId) {
        LOG.info("Deleting all DiagnosticTestProfiles for testId={}", testId);
        repository.deleteAllByTest_Id(testId);
    }
}
