package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import com.dazzle.asklepios.repository.DiagnosticTestProfileRepository;
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
public class DiagnosticTestProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestProfileService.class);
    private final DiagnosticTestProfileRepository repository;

    public DiagnosticTestProfileService(DiagnosticTestProfileRepository repository) {
        this.repository = repository;
    }

    public DiagnosticTestProfile create(DiagnosticTestProfile entity) {
        Long testId = entity.getTest() != null ? entity.getTest().getId() : null;
        LOG.info("Creating DiagnosticTestProfile for testId={}", testId);

        validate(entity);

        DiagnosticTestProfile saved = repository.save(entity);

        // enforce single default per test
        if (Boolean.TRUE.equals(saved.getIsDefault())) {
            unsetOtherDefaults(saved.getId(), testId);
        }

        return saved;
    }

    public Optional<DiagnosticTestProfile> update(Long id, DiagnosticTestProfile entity) {
        LOG.info("Updating DiagnosticTestProfile id={}", id);

        return repository.findById(id).map(existing -> {
            entity.setId(existing.getId());

            validate(entity);

            DiagnosticTestProfile saved = repository.save(entity);

            // enforce single default per test
            Long testId = saved.getTest() != null ? saved.getTest().getId() : null;
            if (Boolean.TRUE.equals(saved.getIsDefault())) {
                unsetOtherDefaults(saved.getId(), testId);
            }

            return saved;
        });
    }

    @Transactional(readOnly = true)
    public Page<DiagnosticTestProfile> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<DiagnosticTestProfile> findAllByTestId(Long testId, Pageable pageable) {
        return repository.findAllByTest_Id(testId, pageable);
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

    @Transactional(readOnly = true)
    public Page<DiagnosticTestProfile> findProfilesForLab(Long testId, Pageable pageable) {
        long c = repository.countByTest_Id(testId);
        if (c <= 1) {
            return repository.findAllByTest_Id(testId, pageable);
        }
        return repository.findAllByTest_IdAndIsDefaultFalse(testId, pageable);
    }

    public void deleteAllByTestId(Long testId) {
        LOG.info("Deleting all DiagnosticTestProfiles for testId={}", testId);
        repository.deleteAllByTest_Id(testId);
    }

    // -------------------------
    // helpers
    // -------------------------
    private void validate(DiagnosticTestProfile entity) {
        if (entity.getTest() == null || entity.getTest().getId() == null) {
            throw new BadRequestAlertException("testId is required", "diagnosticTestProfile", "testidmissing");
        }
        if (entity.getName() == null || entity.getName().isBlank()) {
            throw new BadRequestAlertException("name is required", "diagnosticTestProfile", "namemissing");
        }
        if (entity.getResultType() == null) {
            throw new BadRequestAlertException("resultType is required", "diagnosticTestProfile", "resulttypemissing");
        }
        // إذا بتحبي: default قيمة لو null (بدل error)
        // if (entity.getResultType() == null) entity.setResultType(TestResultType.TEXT);
    }

    private void unsetOtherDefaults(Long savedProfileId, Long testId) {
        if (testId == null) return;

        // ملاحظة: هذا يعتمد على method جديد بالـ repository (انظر تحت)
        repository.unsetDefaultsExcept(testId, savedProfileId);
    }
}
