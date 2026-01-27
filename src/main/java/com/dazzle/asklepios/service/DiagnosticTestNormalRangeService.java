// src/main/java/com/dazzle/asklepios/service/DiagnosticTestNormalRangeService.java
package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTestNormalRange;
import com.dazzle.asklepios.domain.DiagnosticTestNormalRangeLov;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import com.dazzle.asklepios.repository.DiagnosticTestNormalRangeLovRepository;
import com.dazzle.asklepios.repository.DiagnosticTestNormalRangeRepository;
import com.dazzle.asklepios.repository.DiagnosticTestProfileRepository;
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

    /**
     * Creates a new service instance.
     *
     * @param rangeRepository repository for normal ranges
     * @param lovRepository repository for normal range LOV rows
     * @param profileRepository repository for profiles (to validate and load managed profile)
     */
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
     * <p>Requires {@code profileTestId}. The profile is loaded to ensure it exists and to align {@code test_id}
     * with {@code profile.test}. If the profile result type is {@link TestResultType#LOV}, LOV keys (if provided)
     * are persisted into the LOV table.</p>
     *
     * @param entity new normal range entity
     * @return persisted entity (with id)
     * @throws IllegalArgumentException if profileTestId is missing or profile does not exist
     */
    public DiagnosticTestNormalRange create(DiagnosticTestNormalRange entity) {
        if (entity.getProfileTest() == null || entity.getProfileTest().getId() == null) {
            throw new IllegalArgumentException("profileTestId is required");
        }

        DiagnosticTestProfile profile = profileRepository.findById(entity.getProfileTest().getId())
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + entity.getProfileTest().getId()));

        entity.setProfileTest(profile);

        // keep test_id aligned with profile->test (reference only)
        entity.setTest(profile.getTest());

        DiagnosticTestNormalRange saved = rangeRepository.save(entity);

        persistLovsIfNeeded(saved, profile.getResultType(), entity.getLovKeys());

        LOG.debug("[NormalRange] CREATE - saved id={} profileTestId={} testId={}",
                saved.getId(),
                saved.getProfileTest() != null ? saved.getProfileTest().getId() : null,
                saved.getTest() != null ? saved.getTest().getId() : null);

        return saved;
    }

    /**
     * Updates an existing normal range definition by id.
     *
     * <p>Replaces persisted LOVs for the range if the profile is {@link TestResultType#LOV}.</p>
     *
     * @param id range id
     * @param entity update payload (must include profileTestId)
     * @return updated entity if found, otherwise empty
     * @throws IllegalArgumentException if profileTestId is missing or profile does not exist
     */
    public Optional<DiagnosticTestNormalRange> update(Long id, DiagnosticTestNormalRange entity) {
        return rangeRepository.findById(id).map(existing -> {

            if (entity.getProfileTest() == null || entity.getProfileTest().getId() == null) {
                throw new IllegalArgumentException("profileTestId is required");
            }

            DiagnosticTestProfile profile = profileRepository.findById(entity.getProfileTest().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + entity.getProfileTest().getId()));

            entity.setId(id);
            entity.setProfileTest(profile);

            // keep reference test_id aligned with profile->test
            entity.setTest(profile.getTest());

            DiagnosticTestNormalRange updated = rangeRepository.save(entity);

            // refresh LOVs
            lovRepository.deleteByNormalRangeId(id);
            persistLovsIfNeeded(updated, profile.getResultType(), entity.getLovKeys());

            LOG.debug("[NormalRange] UPDATE - updated id={} profileTestId={} testId={}",
                    updated.getId(),
                    updated.getProfileTest() != null ? updated.getProfileTest().getId() : null,
                    updated.getTest() != null ? updated.getTest().getId() : null);

            return updated;
        });
    }

    /**
     * Returns a paginated list of all normal ranges.
     * LOV keys are loaded into {@code lovKeys}.
     *
     * @param pageable pagination and sorting
     * @return page of normal ranges
     */
    public Page<DiagnosticTestNormalRange> findAll(Pageable pageable) {
        return rangeRepository.findAll(pageable)
                .map(range -> {
                    range.setLovKeys(findLovs(range.getId()));
                    return range;
                });
    }

    /**
     * Returns a single normal range by id.
     * LOV keys are loaded into {@code lovKeys}.
     *
     * @param id range id
     * @return optional range
     */
    public Optional<DiagnosticTestNormalRange> findOne(Long id) {
        return rangeRepository.findById(id)
                .map(range -> {
                    range.setLovKeys(findLovs(id));
                    return range;
                });
    }

    /**
     * Returns a paginated list of normal ranges by diagnostic test id (legacy/reporting use).
     * LOV keys are loaded into {@code lovKeys}.
     *
     * @param testId diagnostic test id
     * @param pageable pagination and sorting
     * @return page of normal ranges
     */
    public Page<DiagnosticTestNormalRange> findAllByTestId(Long testId, Pageable pageable) {
        return rangeRepository.findByTest_Id(testId, pageable)
                .map(range -> {
                    range.setLovKeys(findLovs(range.getId()));
                    return range;
                });
    }

    /**
     * Returns a paginated list of normal ranges by profile test id (primary use).
     * LOV keys are loaded into {@code lovKeys}.
     *
     * @param profileTestId profile test id
     * @param pageable pagination and sorting
     * @return page of normal ranges
     */
    public Page<DiagnosticTestNormalRange> findAllByProfileTestId(Long profileTestId, Pageable pageable) {
        return rangeRepository.findByProfileTest_Id(profileTestId, pageable)
                .map(range -> {
                    range.setLovKeys(findLovs(range.getId()));
                    return range;
                });
    }
    /**
     * Returns all normal ranges for a given profileTestId (non-paginated).
     *
     * @param profileTestId profile test id
     * @return list of matching normal ranges (may be empty)
     */
    public List<DiagnosticTestNormalRange> findListByProfileTestId(Long profileTestId) {
        return rangeRepository.findAllByProfileTest_Id(profileTestId).stream()
                .peek(r -> r.setLovKeys(findLovs(r.getId())))
                .toList();
    }

    /**
     * Returns all normal ranges for a profile test id (non-paginated).
     * Intended for internal service-to-service use.
     *
     * @param profileTestId profile test id
     * @return list of normal ranges with {@code lovKeys} populated
     */
    public List<DiagnosticTestNormalRange> findAllByProfileTestId(Long profileTestId) {
        return rangeRepository.findAllByProfileTest_Id(profileTestId).stream()
                .peek(r -> r.setLovKeys(findLovs(r.getId())))
                .toList();
    }

    /**
     * Deletes a normal range and its LOV rows.
     *
     * @param id range id
     */
    public void delete(Long id) {
        LOG.debug("[NormalRange] DELETE - start. id={}", id);
        lovRepository.deleteByNormalRangeId(id);
        rangeRepository.deleteById(id);
        LOG.debug("[NormalRange] DELETE - done. id={}", id);
    }

    /**
     * Returns LOV keys associated with a normal range id.
     *
     * @param normalRangeId normal range id
     * @return list of LOV keys (may be empty)
     */
    public List<String> findLovsByNormalRangeId(Long normalRangeId) {
        return findLovs(normalRangeId);
    }

    private List<String> findLovs(Long normalRangeId) {
        return lovRepository.findByNormalRangeId(normalRangeId)
                .stream()
                .map(DiagnosticTestNormalRangeLov::getLov)
                .toList();
    }

    private void persistLovsIfNeeded(DiagnosticTestNormalRange range, TestResultType resultType, List<String> lovKeys) {
        if (resultType != TestResultType.LOV) return;
        if (lovKeys == null || lovKeys.isEmpty()) return;

        List<DiagnosticTestNormalRangeLov> lovs = lovKeys.stream()
                .map(key -> DiagnosticTestNormalRangeLov.builder()
                        .normalRange(range)
                        .lov(key)
                        .build())
                .toList();

        lovRepository.saveAll(lovs);
        LOG.debug("[NormalRange] LOV - saved {} keys for rangeId={}", lovs.size(), range.getId());
    }
}
