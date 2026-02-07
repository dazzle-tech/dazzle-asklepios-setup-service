package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
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

    public DiagnosticTestProfileService(
            DiagnosticTestProfileRepository repository,
            DiagnosticTestRepository diagnosticTestRepository
    ) {
        this.repository = repository;
        this.diagnosticTestRepository = diagnosticTestRepository;
    }

    public DiagnosticTestProfile create(DiagnosticTestProfile entity) {
        Long testId = entity.getTest() != null ? entity.getTest().getId() : null;
        LOG.debug("[TestProfile] CREATE - start. testId={} name={} resultType={}",
                testId, entity.getName(), entity.getResultType());

        validate(entity);

        DiagnosticTest test = diagnosticTestRepository.findById(testId)
                .orElseThrow(() -> {
                    LOG.warn("[TestProfile] CREATE - test not found. testId={}", testId);
                    return new BadRequestAlertException("Test not found", "diagnosticTest", "notfound");
                });

        if (test.getType() != TestType.LABORATORY) {
            LOG.warn("[TestProfile] CREATE - not laboratory. testId={} type={}", testId, test.getType());
            throw new BadRequestAlertException(
                    "Profiles are allowed only for LABORATORY tests",
                    "diagnosticTestProfile",
                    "not_laboratory"
            );
        }

        entity.setIsDefault(false);
        entity.setTest(test);

        DiagnosticTestProfile saved = repository.save(entity);

        LOG.info("[TestProfile] CREATE - done. id={} testId={} isDefault={} isActive={}",
                saved.getId(),
                saved.getTest() != null ? saved.getTest().getId() : null,
                saved.getIsDefault(),
                saved.getIsActive());

        return saved;
    }

    public Optional<DiagnosticTestProfile> update(Long id, DiagnosticTestProfile entity) {
        LOG.debug("[TestProfile] UPDATE - start. id={} name={} resultType={}",
                id, entity.getName(), entity.getResultType());

        Optional<DiagnosticTestProfile> out = repository.findById(id).map(existing -> {

            if (Boolean.TRUE.equals(existing.getIsDefault())) {
                LOG.warn("[TestProfile] UPDATE - default profile readonly. id={}", id);
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

            DiagnosticTestProfile saved = repository.save(entity);

            LOG.info("[TestProfile] UPDATE - done. id={} testId={} isDefault={} isActive={}",
                    saved.getId(),
                    saved.getTest() != null ? saved.getTest().getId() : null,
                    saved.getIsDefault(),
                    saved.getIsActive());

            return saved;
        });

        LOG.debug("[TestProfile] UPDATE - end. id={} found={}", id, out.isPresent());
        return out;
    }

    @Transactional(readOnly = true)
    public Page<DiagnosticTestProfile> findAll(Pageable pageable) {
        LOG.debug("[TestProfile] FIND_ALL - pageable={}", pageable);

        Page<DiagnosticTestProfile> page = repository.findAll(pageable);

        LOG.info("[TestProfile] FIND_ALL - returned {} items (page {} of {})",
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());

        return page;
    }

    @Transactional(readOnly = true)
    public Page<DiagnosticTestProfile> findAllByTestId(Long testId, Pageable pageable) {
        LOG.debug("[TestProfile] FIND_BY_TEST - testId={} pageable={}", testId, pageable);

        Page<DiagnosticTestProfile> page = repository.findAllByTest_Id(testId, pageable);

        LOG.info("[TestProfile] FIND_BY_TEST - testId={} returned {} items (page {} of {})",
                testId, page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());

        return page;
    }

    @Transactional(readOnly = true)
    public Optional<DiagnosticTestProfile> findOne(Long id) {
        LOG.debug("[TestProfile] FIND_ONE - id={}", id);

        Optional<DiagnosticTestProfile> out = repository.findById(id);

        LOG.info("[TestProfile] FIND_ONE - id={} found={}", id, out.isPresent());
        return out;
    }

    public void delete(Long id) {
        LOG.debug("[TestProfile] DELETE - start. id={}", id);

        if (!repository.existsById(id)) {
            LOG.warn("[TestProfile] DELETE - not found. id={}", id);
            throw new BadRequestAlertException("Profile not found", "diagnosticTestProfile", "notfound");
        }

        repository.deleteById(id);
        LOG.info("[TestProfile] DELETE - done. id={}", id);
    }

    @Transactional(readOnly = true)
    public List<DiagnosticTestProfile> findProfilesForLab(Long testId) {
        LOG.debug("[TestProfile] FIND_FOR_LAB - start. testId={}", testId);

        long countProfile = repository.countByTest_Id(testId);
        LOG.debug("[TestProfile] FIND_FOR_LAB - profilesCount={}", countProfile);

        List<DiagnosticTestProfile> list;
        if (countProfile <= 1) {
            list = repository.findAllByTest_IdAndIsActiveTrue(testId);
            LOG.debug("[TestProfile] FIND_FOR_LAB - mode=ACTIVE_ONLY (count<=1)");
        } else {
            list = repository.findAllByTest_IdAndIsActiveTrueAndIsDefaultFalse(testId);
            LOG.debug("[TestProfile] FIND_FOR_LAB - mode=ACTIVE_NON_DEFAULT (count>1)");
        }

        LOG.info("[TestProfile] FIND_FOR_LAB - done. testId={} size={}", testId, list.size());
        return list;
    }

    @Transactional(readOnly = true)
    public Map<Long, List<DiagnosticTestProfile>> findActiveProfilesForLabByTestIds(Collection<Long> testIds) {
        int requestedTestIdsCount = (testIds == null) ? 0 : testIds.size();
        LOG.debug("[TestProfile] FIND_ACTIVE_BY_TEST_IDS - start. requestedTestIdsCount={}", requestedTestIdsCount);

        if (testIds == null || testIds.isEmpty()) {
            LOG.info("[TestProfile] FIND_ACTIVE_BY_TEST_IDS - empty input. returning empty map");
            return Map.of();
        }

        List<Long> distinctNonNullTestIds = testIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (distinctNonNullTestIds.isEmpty()) {
            LOG.info("[TestProfile] FIND_ACTIVE_BY_TEST_IDS - all ids were null. returning empty map");
            return Map.of();
        }

        Map<Long, Long> profileCountByTestId = repository.countByTestIds(distinctNonNullTestIds).stream()
                .collect(Collectors.toMap(TestProfileCountVM::testId, TestProfileCountVM::count));

        LOG.debug("[TestProfile] FIND_ACTIVE_BY_TEST_IDS - loaded profile counts. distinctTestIds={} countsSize={}",
                distinctNonNullTestIds.size(), profileCountByTestId.size());

        List<Long> testIdsWithZeroOrOneProfile = distinctNonNullTestIds.stream()
                .filter(testId -> profileCountByTestId.getOrDefault(testId, 0L) <= 1)
                .toList();

        List<Long> testIdsWithMoreThanOneProfile = distinctNonNullTestIds.stream()
                .filter(testId -> profileCountByTestId.getOrDefault(testId, 0L) > 1)
                .toList();

        List<DiagnosticTestProfile> activeProfilesForTestsWithZeroOrOneProfile = testIdsWithZeroOrOneProfile.isEmpty()
                ? List.of()
                : repository.findAllByTest_IdInAndIsActiveTrue(testIdsWithZeroOrOneProfile);

        List<DiagnosticTestProfile> activeNonDefaultProfilesForTestsWithMultipleProfiles = testIdsWithMoreThanOneProfile.isEmpty()
                ? List.of()
                : repository.findAllByTest_IdInAndIsActiveTrueAndIsDefaultFalse(testIdsWithMoreThanOneProfile);

        Map<Long, List<DiagnosticTestProfile>> profilesGroupedByTestId =
                Stream.concat(
                                activeProfilesForTestsWithZeroOrOneProfile.stream(),
                                activeNonDefaultProfilesForTestsWithMultipleProfiles.stream()
                        )
                        .collect(Collectors.groupingBy(profile -> profile.getTest().getId()));

        LOG.info("[TestProfile] FIND_ACTIVE_BY_TEST_IDS - done. distinctTestIds={} groupedTestIds={}",
                distinctNonNullTestIds.size(), profilesGroupedByTestId.size());

        return profilesGroupedByTestId;
    }

    public void deleteAllByTestId(Long testId) {
        LOG.debug("[TestProfile] DELETE_ALL_BY_TEST - start. testId={}", testId);
        repository.deleteAllByTest_Id(testId);
        LOG.info("[TestProfile] DELETE_ALL_BY_TEST - done. testId={}", testId);
    }

    public Optional<TestResultType> getResultTypeById(Long id) {
        LOG.debug("[TestProfile] GET_RESULT_TYPE - id={}", id);

        Optional<TestResultType> out = repository.findById(id).map(DiagnosticTestProfile::getResultType);

        LOG.info("[TestProfile] GET_RESULT_TYPE - id={} found={} value={}",
                id, out.isPresent(), out.orElse(null));

        return out;
    }


    public Optional<DiagnosticTestProfile> toggleIsActive(Long id) {
        LOG.debug("[TestProfile] TOGGLE_ACTIVE - start. id={}", id);

        Optional<DiagnosticTestProfile> out = repository.findById(id)
                .map(p -> {
                    boolean before = Boolean.TRUE.equals(p.getIsActive());
                    p.setIsActive(!before);

                    DiagnosticTestProfile saved = repository.save(p);

                    LOG.info("[TestProfile] TOGGLE_ACTIVE - done. id={} before={} after={}",
                            saved.getId(), before, saved.getIsActive());

                    return saved;
                });

        LOG.debug("[TestProfile] TOGGLE_ACTIVE - end. id={} found={}", id, out.isPresent());
        return out;
    }

    // -------------------------
    // helpers
    // -------------------------
    private void validate(DiagnosticTestProfile entity) {
        Long testId = entity.getTest() != null ? entity.getTest().getId() : null;

        LOG.debug("[TestProfile] VALIDATE - start. testId={} name={} resultType={}",
                testId, entity.getName(), entity.getResultType());

        if (testId == null) {
            LOG.warn("[TestProfile] VALIDATE - missing testId");
            throw new BadRequestAlertException("testId is required", "diagnosticTestProfile", "testidmissing");
        }
        if (entity.getName() == null || entity.getName().isBlank()) {
            LOG.warn("[TestProfile] VALIDATE - missing name. testId={}", testId);
            throw new BadRequestAlertException("name is required", "diagnosticTestProfile", "namemissing");
        }
        if (entity.getResultType() == null) {
            LOG.warn("[TestProfile] VALIDATE - missing resultType. testId={}", testId);
            throw new BadRequestAlertException("resultType is required", "diagnosticTestProfile", "resulttypemissing");
        }

        LOG.debug("[TestProfile] VALIDATE - ok. testId={}", testId);
    }
}
