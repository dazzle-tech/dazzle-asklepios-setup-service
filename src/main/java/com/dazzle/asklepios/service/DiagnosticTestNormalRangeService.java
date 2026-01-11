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

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------
    public DiagnosticTestNormalRange create(DiagnosticTestNormalRange entity) {
        if (entity.getProfileTest() == null || entity.getProfileTest().getId() == null) {
            throw new IllegalArgumentException("profileTestId is required");
        }

        // load profile to guarantee resultType/test are available (and managed)
        DiagnosticTestProfile profile = profileRepository.findById(entity.getProfileTest().getId())
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + entity.getProfileTest().getId()));

        entity.setProfileTest(profile);

        // fill test_id as reference (optional but consistent)
        entity.setTest(profile.getTest());

        DiagnosticTestNormalRange saved = rangeRepository.save(entity);

        persistLovsIfNeeded(saved, profile.getResultType(), entity.getLovKeys());

        return saved;
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------
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

            // Refresh LOVs
            lovRepository.deleteByNormalRangeId(id);

            persistLovsIfNeeded(updated, profile.getResultType(), entity.getLovKeys());

            return updated;
        });
    }

    // -----------------------------------------------------------------------
    // GET ALL (Paginated)
    // -----------------------------------------------------------------------
    public Page<DiagnosticTestNormalRange> findAll(Pageable pageable) {
        return rangeRepository.findAll(pageable)
                .map(range -> {
                    range.setLovKeys(findLovs(range.getId()));
                    return range;
                });
    }

    // -----------------------------------------------------------------------
    // GET ONE
    // -----------------------------------------------------------------------
    public Optional<DiagnosticTestNormalRange> findOne(Long id) {
        return rangeRepository.findById(id)
                .map(range -> {
                    range.setLovKeys(findLovs(id));
                    return range;
                });
    }

    // -----------------------------------------------------------------------
    // GET BY TEST ID (Paginated) - Legacy/Reporting
    // -----------------------------------------------------------------------
    public Page<DiagnosticTestNormalRange> findAllByTestId(Long testId, Pageable pageable) {
        return rangeRepository.findByTest_Id(testId, pageable)
                .map(range -> {
                    range.setLovKeys(findLovs(range.getId()));
                    return range;
                });
    }

    // -----------------------------------------------------------------------
    // GET BY PROFILE TEST ID (Paginated) - Primary
    // -----------------------------------------------------------------------
    public Page<DiagnosticTestNormalRange> findAllByProfileTestId(Long profileTestId, Pageable pageable) {
        return rangeRepository.findByProfileTest_Id(profileTestId, pageable)
                .map(range -> {
                    range.setLovKeys(findLovs(range.getId()));
                    return range;
                });
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------
    public void delete(Long id) {
        LOG.debug("Delete DiagnosticTestNormalRange id={}", id);
        lovRepository.deleteByNormalRangeId(id);
        rangeRepository.deleteById(id);
    }

    // -----------------------------------------------------------------------
    // GET LOVs BY RANGE ID
    // -----------------------------------------------------------------------
    public List<String> findLovsByNormalRangeId(Long normalRangeId) {
        return findLovs(normalRangeId);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    private List<String> findLovs(Long normalRangeId) {
        return lovRepository.findByNormalRangeId(normalRangeId)
                .stream()
                .map(DiagnosticTestNormalRangeLov::getLov)
                .toList();
    }

    private void persistLovsIfNeeded(DiagnosticTestNormalRange range, TestResultType resultType, List<String> lovKeys) {
        if (resultType != TestResultType.LOV) {
            return;
        }
        if (lovKeys == null || lovKeys.isEmpty()) {
            return;
        }

        List<DiagnosticTestNormalRangeLov> lovs = lovKeys.stream()
                .map(key -> DiagnosticTestNormalRangeLov.builder()
                        .normalRange(range)
                        .lov(key)
                        .build())
                .toList();

        lovRepository.saveAll(lovs);
        LOG.debug("Saved {} LOV keys for rangeId={}", lovs.size(), range.getId());
    }
}
