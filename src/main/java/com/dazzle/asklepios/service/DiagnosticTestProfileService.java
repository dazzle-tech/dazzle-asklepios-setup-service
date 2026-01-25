package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.repository.DiagnosticTestProfileRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.profile.TestProfileCountVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class DiagnosticTestProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestProfileService.class);
    private final DiagnosticTestProfileRepository repository;
    private final DiagnosticTestRepository diagnosticTestRepository;

    public DiagnosticTestProfileService(DiagnosticTestProfileRepository repository, DiagnosticTestRepository diagnosticTestRepository) {
        this.repository = repository;
        this.diagnosticTestRepository = diagnosticTestRepository;
    }

    public DiagnosticTestProfile create(DiagnosticTestProfile entity) {
        Long testId = entity.getTest() != null ? entity.getTest().getId() : null;
        LOG.info("Creating DiagnosticTestProfile for testId={}", testId);

        validate(entity);


        DiagnosticTest test = diagnosticTestRepository.findById(testId)
                .orElseThrow(() -> new BadRequestAlertException("Test not found", "diagnosticTest", "notfound"));

        if (test.getType() != TestType.LABORATORY) {
            throw new BadRequestAlertException(
                    "Profiles are allowed only for LABORATORY tests",
                    "diagnosticTestProfile",
                    "not_laboratory"
            );
        }


        entity.setIsDefault(false);


        entity.setTest(test);

        DiagnosticTestProfile saved = repository.save(entity);

        return saved;
    }

    public Optional<DiagnosticTestProfile> update(Long id, DiagnosticTestProfile entity) {
        LOG.info("Updating DiagnosticTestProfile id={}", id);

        return repository.findById(id).map(existing -> {


            if (Boolean.TRUE.equals(existing.getIsDefault())) {
                throw new BadRequestAlertException(
                        "default_profile_readonly",
                        "diagnosticTestProfile",
                        "Default profile cannot be updated"
                );
            }

            entity.setIsDefault(false);
            entity.setId(existing.getId());
            entity.setTest(existing.getTest());
            validate(entity);

            if (entity.getIsActive() == null) {
                entity.setIsActive(existing.getIsActive() != null ? existing.getIsActive() : true);
            }

            return repository.save(entity);
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


    @Transactional(readOnly = true)
    public Map<Long, List<DiagnosticTestProfile>> findActiveProfilesForLabByTestIds(Collection<Long> testIds) {
        if (testIds == null || testIds.isEmpty()) return Map.of();

        List<Long> ids = testIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return Map.of();

        Map<Long, Long> countsById = repository.countByTestIds(ids).stream()
                .collect(Collectors.toMap(TestProfileCountVM::testId, TestProfileCountVM::cnt));

        List<Long> idsCountLE1 = ids.stream().filter(id -> countsById.getOrDefault(id, 0L) <= 1).toList();
        List<Long> idsCountGT1 = ids.stream().filter(id -> countsById.getOrDefault(id, 0L) > 1).toList();

        List<DiagnosticTestProfile> allForLE1 = idsCountLE1.isEmpty()
                ? List.of()
                : repository.findAllByTest_IdInAndIsActiveTrue(idsCountLE1);

        List<DiagnosticTestProfile> nonDefaultForGT1 = idsCountGT1.isEmpty()
                ? List.of()
                : repository.findAllByTest_IdInAndIsActiveTrueAndIsDefaultFalse(idsCountGT1);

        return Stream.concat(allForLE1.stream(), nonDefaultForGT1.stream())
                .collect(Collectors.groupingBy(p -> p.getTest().getId()));
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


    }

    private void unsetOtherDefaults(Long savedProfileId, Long testId) {
        if (testId == null) return;


        repository.unsetDefaultsExcept(testId, savedProfileId);

    }

    public Optional<DiagnosticTestProfile> toggleIsActive(Long id) {
        return repository.findById(id)
                .map(p -> {
                    p.setIsActive(!Boolean.TRUE.equals(p.getIsActive()));
                    return repository.save(p);
                });
    }
}
