package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTestNormalRange;
import com.dazzle.asklepios.domain.DiagnosticTestNormalRangeLov;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import com.dazzle.asklepios.repository.DiagnosticTestNormalRangeLovRepository;
import com.dazzle.asklepios.repository.DiagnosticTestNormalRangeRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DiagnosticTestNormalRangeService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestNormalRangeService.class);

    private final DiagnosticTestNormalRangeRepository rangeRepository;
    private final DiagnosticTestNormalRangeLovRepository lovRepository;

    public DiagnosticTestNormalRangeService(
            DiagnosticTestNormalRangeRepository rangeRepository,
            DiagnosticTestNormalRangeLovRepository lovRepository
    ) {
        this.rangeRepository = rangeRepository;
        this.lovRepository = lovRepository;
    }

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------
    public DiagnosticTestNormalRange create(DiagnosticTestNormalRange entity) {
        LOG.debug("Create DiagnosticTestNormalRange: {}", entity);
        DiagnosticTestNormalRange saved = rangeRepository.save(entity);

        // Handle LOVs only if type == LOV
        if (entity.getResultType() == TestResultType.LOV &&
                entity.getLovKeys() != null && !entity.getLovKeys().isEmpty()) {

            List<DiagnosticTestNormalRangeLov> lovs = entity.getLovKeys().stream()
                    .map(key -> DiagnosticTestNormalRangeLov.builder()
                            .normalRange(saved)
                            .lov(key)
                            .build())
                    .collect(Collectors.toList());

            lovRepository.saveAll(lovs);
            LOG.debug("Saved {} LOV keys for rangeId={}", lovs.size(), saved.getId());
        }

        return saved;
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------
    public Optional<DiagnosticTestNormalRange> update(Long id, DiagnosticTestNormalRange entity) {
        return rangeRepository.findById(id).map(existing -> {
            entity.setId(id);
            DiagnosticTestNormalRange updated = rangeRepository.save(entity);

            // Refresh LOVs
            lovRepository.deleteByNormalRangeId(id);

            if (entity.getResultType() == TestResultType.LOV &&
                    entity.getLovKeys() != null && !entity.getLovKeys().isEmpty()) {

                List<DiagnosticTestNormalRangeLov> lovs = entity.getLovKeys().stream()
                        .map(key -> DiagnosticTestNormalRangeLov.builder()
                                .normalRange(updated)
                                .lov(key)
                                .build())
                        .collect(Collectors.toList());

                lovRepository.saveAll(lovs);
                LOG.debug("Updated {} LOV keys for rangeId={}", lovs.size(), id);
            }

            return updated;
        });
    }

    // -----------------------------------------------------------------------
    // GET ALL (Paginated)
    // -----------------------------------------------------------------------
    public Page<DiagnosticTestNormalRange> findAll(Pageable pageable) {
        return rangeRepository.findAll(pageable)
                .map(range -> {
                    List<String> lovs = lovRepository.findByNormalRangeId(range.getId())
                            .stream()
                            .map(DiagnosticTestNormalRangeLov::getLov)
                            .toList();
                    range.setLovKeys(lovs);
                    return range;
                });
    }

    // -----------------------------------------------------------------------
    // GET ONE
    // -----------------------------------------------------------------------
    public Optional<DiagnosticTestNormalRange> findOne(Long id) {
        return rangeRepository.findById(id)
                .map(range -> {
                    List<String> lovs = lovRepository.findByNormalRangeId(id)
                            .stream()
                            .map(DiagnosticTestNormalRangeLov::getLov)
                            .toList();
                    range.setLovKeys(lovs);
                    return range;
                });
    }

    // -----------------------------------------------------------------------
    // GET BY TEST ID (Paginated)
    // -----------------------------------------------------------------------
    public Page<DiagnosticTestNormalRange> findAllByTestId(Long testId, Pageable pageable) {
        return rangeRepository.findByTest_Id(testId, pageable)
                .map(range -> {
                    List<String> lovs = lovRepository.findByNormalRangeId(range.getId())
                            .stream()
                            .map(DiagnosticTestNormalRangeLov::getLov)
                            .toList();
                    range.setLovKeys(lovs);
                    return range;
                });
    }

    // -----------------------------------------------------------------------
    // GET BY TEST ID (Paginated)
    // -----------------------------------------------------------------------
    public Page<DiagnosticTestNormalRange> findAllByProfileTestId(Long profileTestId, Pageable pageable) {
        return rangeRepository.findByProfileTest_Id(profileTestId, pageable)
                .map(range -> {
                    List<String> lovs = lovRepository.findByNormalRangeId(range.getId())
                            .stream()
                            .map(DiagnosticTestNormalRangeLov::getLov)
                            .toList();
                    range.setLovKeys(lovs);
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
        return lovRepository.findByNormalRangeId(normalRangeId)
                .stream()
                .map(DiagnosticTestNormalRangeLov::getLov)
                .toList();
    }
}
