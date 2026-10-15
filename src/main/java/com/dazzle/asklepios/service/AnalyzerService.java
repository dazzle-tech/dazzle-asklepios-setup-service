package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Analyzer;
import com.dazzle.asklepios.repository.AnalyzerRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class AnalyzerService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyzerService.class);

    private final AnalyzerRepository analyzerRepository;

    public AnalyzerService(AnalyzerRepository analyzerRepository) {
        this.analyzerRepository = analyzerRepository;
    }

    public Analyzer create(Analyzer incoming) {
        LOG.info("[CREATE] Request to create Analyzer payload={}", incoming);

        if (incoming == null) {
            throw new BadRequestAlertException("Analyzer payload is required", "analyzer", "payload.required");
        }
        Analyzer entity = Analyzer.builder()
                .code(incoming.getCode())
                .type(incoming.getType())
                .isActive(Boolean.TRUE.equals(incoming.getIsActive()) || incoming.getIsActive() == null)
                .build();
        try {
            Analyzer saved = analyzerRepository.saveAndFlush(entity);
            LOG.info("Successfully created analyzer id={} code='{}'", saved.getId(), saved.getCode());
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage());
            LOG.error("Database constraint violation while creating analyzer: {}", message, ex);
            throw new BadRequestAlertException(
                    "Database constraint violated while creating analyzer (check required fields).",
                    "analyzer",
                    "db.constraint"
            );
        }
    }

    public Optional<Analyzer> update(Long id, Analyzer incoming) {
        LOG.info("[UPDATE] Request to update Analyzer id={} payload={}", id, incoming);

        if (incoming == null) {
            throw new BadRequestAlertException("Analyzer payload is required", "analyzer", "payload.required");
        }

        Analyzer existing = analyzerRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Analyzer not found with id " + id, "analyzer", "notfound"));
        existing.setCode(incoming.getCode());
        existing.setType(incoming.getType());
        existing.setIsActive(incoming.getIsActive());
        existing.setLastModifiedDate(Instant.now());
        existing.setLastModifiedBy(incoming.getLastModifiedBy());
        try {
            Analyzer updated = analyzerRepository.saveAndFlush(existing);
            LOG.info("Successfully updated analyzer id={} (code='{}')", updated.getId(), updated.getCode());
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage());
            LOG.error("Database constraint violation while updating analyzer: {}", message, ex);
            throw new BadRequestAlertException(
                    "Database constraint violated while updating analyzer (check required fields).",
                    "analyzer",
                    "db.constraint"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<Analyzer> findAll() {
        LOG.debug("Fetching all Analyzers");
        return analyzerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Analyzer> findAll(Pageable pageable) {
        LOG.debug("Fetching paged Analyzers pageable={}", pageable);
        return analyzerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Analyzer> findByType(String type, Pageable pageable) {
        LOG.debug("Fetching Analyzers by type={} pageable={}", type, pageable);
        return analyzerRepository.findByType(type, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Analyzer> findByCodeContainingIgnoreCase(String code, Pageable pageable) {
        LOG.debug("Fetching Analyzers by code like='{}' pageable={}", code, pageable);
        return analyzerRepository.findByCodeContainingIgnoreCase(code, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Analyzer> findOne(Long id) {
        LOG.debug("Fetching single Analyzer by id={}", id);
        return analyzerRepository.findById(id);
    }

    public Optional<Analyzer> toggleIsActive(Long id) {
        LOG.info("Toggling isActive for Analyzer id={}", id);
        return analyzerRepository.findById(id)
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    Analyzer saved = analyzerRepository.save(entity);
                    LOG.info("Analyzer id={} active status changed to {}", id, saved.getIsActive());
                    return saved;
                });
    }

    public void delete(Long id) {
        LOG.debug("Request to delete Analyzer : {}", id);
        analyzerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Analyzer> findActive(Pageable pageable) {
        LOG.debug("Fetching active Analyzers pageable={}", pageable);
        return analyzerRepository.findByIsActiveTrue(pageable);
    }
}
