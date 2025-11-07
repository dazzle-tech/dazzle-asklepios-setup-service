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
            DiagnosticTestNormalRangeLovRepository lovRepository) {
        this.rangeRepository = rangeRepository;
        this.lovRepository = lovRepository;
    }

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------
    public DiagnosticTestNormalRange create(DiagnosticTestNormalRange entity) {
        LOG.debug("Create DiagnosticTestNormalRange: {}", entity);
        DiagnosticTestNormalRange saved = rangeRepository.save(entity);

        // handle LOV keys if resultType == LOV
        if (entity.getResultType() == TestResultType.LOV && entity.getLovKeys() != null && !entity.getLovKeys().isEmpty()) {
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

            // clear old LOVs and re-insert if necessary
            lovRepository.deleteByNormalRangeId(id);
            if (entity.getResultType() == TestResultType.LOV && entity.getLovKeys() != null && !entity.getLovKeys().isEmpty()) {
                List<DiagnosticTestNormalRangeLov> lovs = entity.getLovKeys().stream()
                        .map(key -> DiagnosticTestNormalRangeLov.builder()
                                .normalRange(updated)
                                .lov(key)
                                .build())
                        .collect(Collectors.toList());
                lovRepository.saveAll(lovs);
                LOG.debug("Updated LOV keys for rangeId={}", id);
            }

            return updated;
        });
    }

    // -----------------------------------------------------------------------
    // GET ALL (Paginated)
    // -----------------------------------------------------------------------
    public Page<DiagnosticTestNormalRange> findAll(Pageable pageable) {
        return rangeRepository.findAll(pageable);
    }

    // -----------------------------------------------------------------------
    // GET ONE
    // -----------------------------------------------------------------------
    public Optional<DiagnosticTestNormalRange> findOne(Long id) {
        return rangeRepository.findById(id);
    }

    // -----------------------------------------------------------------------
    // GET BY TEST ID
    // -----------------------------------------------------------------------
    public Page<DiagnosticTestNormalRange> findAllByTestId(Long testId, Pageable pageable) {
        return rangeRepository.findByTest_Id(testId, pageable);
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------
    public void delete(Long id) {
        LOG.debug("Delete DiagnosticTestNormalRange id={}", id);
        lovRepository.deleteByNormalRangeId(id);
        rangeRepository.deleteById(id);
    }


    public List<String> findLovsByNormalRangeId(Long normalRangeId) {
        return lovRepository.findByNormalRangeId(normalRangeId)
                .stream()
                .map(DiagnosticTestNormalRangeLov::getLov)
                .toList();
    }

}


