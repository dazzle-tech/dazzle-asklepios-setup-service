package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTestNormalRange;
import com.dazzle.asklepios.domain.DiagnosticTestNormalRangeLov;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import com.dazzle.asklepios.repository.DiagnosticTestNormalRangeLovRepository;
import com.dazzle.asklepios.repository.DiagnosticTestNormalRangeRepository;
import com.dazzle.asklepios.repository.DiagnosticTestProfileRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing {@link DiagnosticTestNormalRange} definitions.
 *
 * <p>This service is responsible for:
 * <ul>
 *   <li>Validating that {@code profileTestId} is provided</li>
 *   <li>Ensuring {@code test_id} stays aligned with {@code profileTest.test}</li>
 *   <li>Persisting and refreshing LOV values for {@link TestResultType#LOV} profiles</li>
 *   <li>Loading LOV keys into the transient {@code lovKeys} field for responses</li>
 * </ul>
 * </p>
 */
@Service
@Transactional
public class DiagnosticTestNormalRangeService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestNormalRangeService.class);

    private final DiagnosticTestNormalRangeRepository rangeRepository;
    private final DiagnosticTestNormalRangeLovRepository lovRepository;
    private final DiagnosticTestProfileRepository profileRepository;

    public DiagnosticTestNormalRangeService(
            DiagnosticTestNormalRangeRepository rangeRepository,
            DiagnosticTestNormalRangeLovRepository lovRepository,
            DiagnosticTestProfileRepository profileRepository
    ) {
        this.rangeRepository = rangeRepository;
        this.lovRepository = lovRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Creates a normal range definition.
     *
     * @param entity new normal range entity
     * @return persisted entity (with id)
     */
    public DiagnosticTestNormalRange create(DiagnosticTestNormalRange entity) {
        Long profileTestId = (entity.getProfileTest() != null) ? entity.getProfileTest().getId() : null;
        LOG.debug("[NormalRange] CREATE - start. profileTestId={} entityId={}", profileTestId, entity.getId());

        if (profileTestId == null) {
            LOG.warn("[NormalRange] CREATE - missing profileTestId");
            throw new BadRequestAlertException(
                    "missing_profile_test_id",
                    "diagnosticTestNormalRange",
                    "profileTestId is required"
            );
        }

        DiagnosticTestProfile profile = profileRepository.findById(profileTestId)
                .orElseThrow(() -> {
                    LOG.warn("[NormalRange] CREATE - profile not found. profileTestId={}", profileTestId);
                    return new BadRequestAlertException(
                            "profile_not_found",
                            "diagnosticTestProfile",
                            "Profile not found: " + profileTestId
                    );
                });
        if (profile.getResultType() == TestResultType.TEXT) {

            LOG.warn("[NormalRange] CREATE - TEXT profile does not support normal ranges. profileTestId={}",
                    profileTestId);

            throw new BadRequestAlertException(
                    "Normal range is not supported for TEXT result type",
                    "diagnosticTestNormalRange",
                    "text_result_type"
            );
        }
        entity.setProfileTest(profile);
        entity.setTest(profile.getTest());

        DiagnosticTestNormalRange saved = rangeRepository.save(entity);
        persistLovsIfNeeded(saved, profile.getResultType(), entity.getLovKeys());

        LOG.info("[NormalRange] CREATE - done. id={} profileTestId={} testId={}",
                saved.getId(),
                saved.getProfileTest() != null ? saved.getProfileTest().getId() : null,
                saved.getTest() != null ? saved.getTest().getId() : null);

        return saved;
    }

    /**
     * Updates an existing normal range definition by id.
     *
     * @param id     range id
     * @param entity update payload
     * @return updated entity if found, otherwise empty
     */
    public Optional<DiagnosticTestNormalRange> update(Long id, DiagnosticTestNormalRange entity) {
        Long profileTestId = (entity.getProfileTest() != null) ? entity.getProfileTest().getId() : null;
        LOG.debug("[NormalRange] UPDATE - start. id={} profileTestId={}", id, profileTestId);

        return rangeRepository.findById(id).map(existing -> {

            if (profileTestId == null) {
                LOG.warn("[NormalRange] UPDATE - missing profileTestId. id={}", id);
                throw new BadRequestAlertException(
                        "missing_profile_test_id",
                        "diagnosticTestNormalRange",
                        "profileTestId is required"
                );
            }

            DiagnosticTestProfile profile = profileRepository.findById(profileTestId)
                    .orElseThrow(() -> {
                        LOG.warn("[NormalRange] UPDATE - profile not found. id={} profileTestId={}", id, profileTestId);
                        return new BadRequestAlertException(
                                "profile_not_found",
                                "diagnosticTestProfile",
                                "Profile not found: " + profileTestId
                        );
                    });
            if (profile.getResultType() == TestResultType.TEXT) {

                LOG.warn("[NormalRange] UPDATE - TEXT profile does not support normal ranges. profileTestId={}",
                        profileTestId);

                throw new BadRequestAlertException(
                        "Normal range is not supported for TEXT result type",
                        "diagnosticTestNormalRange",
                        "text_result_type"
                );
            }
            entity.setId(id);
            entity.setProfileTest(profile);
            entity.setTest(profile.getTest());

            DiagnosticTestNormalRange updated = rangeRepository.save(entity);

            lovRepository.deleteByNormalRangeId(id);
            LOG.debug("[NormalRange] UPDATE - cleared LOVs. rangeId={}", id);

            persistLovsIfNeeded(updated, profile.getResultType(), entity.getLovKeys());

            LOG.info("[NormalRange] UPDATE - done. id={} profileTestId={} testId={}",
                    updated.getId(),
                    updated.getProfileTest() != null ? updated.getProfileTest().getId() : null,
                    updated.getTest() != null ? updated.getTest().getId() : null);

            return updated;
        });
    }

    /**
     * Returns a paginated list of all normal ranges (LOV keys populated).
     */
    public Page<DiagnosticTestNormalRange> findAll(Pageable pageable) {
        LOG.debug("[NormalRange] FIND_ALL - pageable={}", pageable);

        Page<DiagnosticTestNormalRange> page = rangeRepository.findAll(pageable)
                .map(range -> {
                    range.setLovKeys(findLovs(range.getId()));
                    return range;
                });

        LOG.info("[NormalRange] FIND_ALL - returned {} items (page {} of {})",
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());

        return page;
    }

    /**
     * Returns a single normal range by id (LOV keys populated).
     */
    public Optional<DiagnosticTestNormalRange> findOne(Long id) {
        LOG.debug("[NormalRange] FIND_ONE - id={}", id);

        Optional<DiagnosticTestNormalRange> out = rangeRepository.findById(id)
                .map(range -> {
                    range.setLovKeys(findLovs(id));
                    return range;
                });

        LOG.info("[NormalRange] FIND_ONE - id={} found={}", id, out.isPresent());
        return out;
    }

    /**
     * Returns a paginated list of normal ranges by diagnostic test id (legacy/reporting use).
     */
    public Page<DiagnosticTestNormalRange> findAllByTestId(Long testId, Pageable pageable) {
        LOG.debug("[NormalRange] FIND_BY_TEST - testId={} pageable={}", testId, pageable);

        Page<DiagnosticTestNormalRange> page = rangeRepository.findByTest_Id(testId, pageable)
                .map(range -> {
                    range.setLovKeys(findLovs(range.getId()));
                    return range;
                });

        LOG.info("[NormalRange] FIND_BY_TEST - testId={} returned {} items (page {} of {})",
                testId, page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());

        return page;
    }

    /**
     * Returns a paginated list of normal ranges by profile test id (primary use).
     */
    public Page<DiagnosticTestNormalRange> findAllByProfileTestId(Long profileTestId, Pageable pageable) {
        LOG.debug("[NormalRange] FIND_BY_PROFILE_TEST - profileTestId={} pageable={}", profileTestId, pageable);

        Page<DiagnosticTestNormalRange> page = rangeRepository.findByProfileTest_Id(profileTestId, pageable)
                .map(range -> {
                    range.setLovKeys(findLovs(range.getId()));
                    return range;
                });

        LOG.info("[NormalRange] FIND_BY_PROFILE_TEST - profileTestId={} returned {} items (page {} of {})",
                profileTestId, page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());

        return page;
    }

    /**
     * Returns all normal ranges for a given profileTestId (non-paginated).
     */
    public List<DiagnosticTestNormalRange> findListByProfileTestId(Long profileTestId) {
        LOG.debug("[NormalRange] LIST_BY_PROFILE_TEST - profileTestId={}", profileTestId);

        List<DiagnosticTestNormalRange> list = rangeRepository.findAllByProfileTest_Id(profileTestId).stream()
                .peek(r -> r.setLovKeys(findLovs(r.getId())))
                .toList();

        LOG.info("[NormalRange] LIST_BY_PROFILE_TEST - profileTestId={} size={}", profileTestId, list.size());
        return list;
    }



    /**
     * Deletes a normal range and its LOV rows.
     */
    public void delete(Long id) {
        LOG.debug("[NormalRange] DELETE - start. id={}", id);

        lovRepository.deleteByNormalRangeId(id);
        LOG.debug("[NormalRange] DELETE - deleted LOVs. id={}", id);

        rangeRepository.deleteById(id);
        LOG.info("[NormalRange] DELETE - done. id={}", id);
    }

    /**
     * Returns LOV keys associated with a normal range id.
     */
    public List<String> findLovsByNormalRangeId(Long normalRangeId) {
        LOG.debug("[NormalRange] FIND_LOVS - normalRangeId={}", normalRangeId);
        List<String> keys = findLovs(normalRangeId);
        LOG.info("[NormalRange] FIND_LOVS - normalRangeId={} size={}", normalRangeId, keys.size());
        return keys;
    }
    /**
     * Loads LOV keys for a given normal range id.
     *
     * <p>Returns an empty list if there are no LOV rows.</p>
     *
     * @param normalRangeId normal range id
     * @return list of LOV keys (may be empty)
     */
    private List<String> findLovs(Long normalRangeId) {
        LOG.debug("[NormalRange] LOV - load keys. normalRangeId={}", normalRangeId);

        List<String> keys = lovRepository.findByNormalRangeId(normalRangeId)
                .stream()
                .map(DiagnosticTestNormalRangeLov::getLov)
                .toList();

        LOG.debug("[NormalRange] LOV - loaded {} keys. normalRangeId={}", keys.size(), normalRangeId);
        return keys;
    }


    private void persistLovsIfNeeded(DiagnosticTestNormalRange range, TestResultType resultType, List<String> lovKeys) {
        if (resultType != TestResultType.LOV) {
            LOG.debug("[NormalRange] LOV - skip (resultType={}) rangeId={}", resultType, range.getId());
            return;
        }
        if (lovKeys == null || lovKeys.isEmpty()) {
            LOG.debug("[NormalRange] LOV - skip (no keys) rangeId={}", range.getId());
            return;
        }

        List<DiagnosticTestNormalRangeLov> lovs = lovKeys.stream()
                .map(key -> DiagnosticTestNormalRangeLov.builder()
                        .normalRange(range)
                        .lov(key)
                        .build())
                .toList();

        lovRepository.saveAll(lovs);
        LOG.info("[NormalRange] LOV - saved {} keys for rangeId={}", lovs.size(), range.getId());
    }
}
