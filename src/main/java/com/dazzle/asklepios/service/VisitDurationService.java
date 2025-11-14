package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.VisitDuration;
import com.dazzle.asklepios.domain.enumeration.VisitType;
import com.dazzle.asklepios.repository.VisitDurationRepository;
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

import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class VisitDurationService {

    private static final Logger LOG = LoggerFactory.getLogger(VisitDurationService.class);

    private final VisitDurationRepository visitDurationRepository;

    public VisitDurationService(VisitDurationRepository visitDurationRepository) {
        this.visitDurationRepository = visitDurationRepository;
    }

    public VisitDuration create(VisitDuration incoming) {
        LOG.info("[CREATE] Request to create VisitDuration payload={}", incoming);

        if (incoming == null) {
            throw new BadRequestAlertException(
                    "VisitDuration payload is required",
                    "visitDuration",
                    "payload.required"
            );
        }

        VisitDuration entity = VisitDuration.builder()
                .visitType(incoming.getVisitType())
                .durationInMinutes(incoming.getDurationInMinutes())
                .resourceSpecific(
                        incoming.getResourceSpecific() != null ? incoming.getResourceSpecific() : Boolean.FALSE
                )
                .build();

        try {
            VisitDuration saved = visitDurationRepository.saveAndFlush(entity);
            LOG.info(
                    "Successfully created VisitDuration id={} visitType={} durationInMinutes={} resourceSpecific={}",
                    saved.getId(),
                    saved.getVisitType(),
                    saved.getDurationInMinutes(),
                    saved.getResourceSpecific()
            );
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            handleConstraintViolationOnSave("create", constraintException);
            throw constraintException;
        }
    }

    public Optional<VisitDuration> update(Long id, VisitDuration incoming) {
        LOG.info("[UPDATE] Request to update VisitDuration id={} payload={}", id, incoming);

        if (incoming == null) {
            throw new BadRequestAlertException(
                    "VisitDuration payload is required",
                    "visitDuration",
                    "payload.required"
            );
        }

        VisitDuration existing = visitDurationRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundAlertException(
                                "VisitDuration not found with id " + id,
                                "visitDuration",
                                "notfound"
                        )
                );

        existing.setVisitType(incoming.getVisitType());
        existing.setDurationInMinutes(incoming.getDurationInMinutes());
        existing.setResourceSpecific(
                incoming.getResourceSpecific() != null ? incoming.getResourceSpecific() : Boolean.FALSE
        );

        try {
            VisitDuration updated = visitDurationRepository.saveAndFlush(existing);
            LOG.info(
                    "Successfully updated VisitDuration id={} visitType={} durationInMinutes={} resourceSpecific={}",
                    updated.getId(),
                    updated.getVisitType(),
                    updated.getDurationInMinutes(),
                    updated.getResourceSpecific()
            );
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            handleConstraintViolationOnSave("update", constraintException);
            throw constraintException;
        }
    }

    @Transactional(readOnly = true)
    public Page<VisitDuration> findAll(Pageable pageable) {
        LOG.debug("Fetching paged VisitDurations pageable={}", pageable);
        return visitDurationRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<VisitDuration> findByVisitType(VisitType visitType, Pageable pageable) {
        LOG.debug("Fetching VisitDurations by visitType='{}' pageable={}", visitType, pageable);
        if (visitType == null) {
            throw new BadRequestAlertException(
                    "Visit type is required",
                    "visitDuration",
                    "visitType.required"
            );
        }
        return visitDurationRepository.findByVisitType(visitType, pageable);
    }
    public boolean delete(Long id) {
        LOG.debug("Request to delete VisitDuration : {}", id);

        if (id == null) {
            LOG.warn("Delete request failed — VisitDuration id is null");
            return false;
        }

        if (!visitDurationRepository.existsById(id)) {
            LOG.warn("Delete request failed — VisitDuration not found with id={}", id);
            return false;
        }

        try {
            visitDurationRepository.deleteById(id);
            LOG.info("Successfully deleted VisitDuration with id={}", id);
            return true;
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            Throwable root = getRootCause(constraintException);
            String message = (root != null ? root.getMessage() : constraintException.getMessage());
            LOG.error("Database constraint violation while deleting VisitDuration id={}: {}", id, message, constraintException);
            return false;
        } catch (Exception constraintException) {
            LOG.error("Unexpected error occurred while deleting VisitDuration id={}: {}", id, constraintException.getMessage(), constraintException);
            return false;
        }
    }

    private void handleConstraintViolationOnSave(String operation, Exception constraintException) {
        Throwable root = getRootCause(constraintException);
        String message = (root != null ? root.getMessage() : constraintException.getMessage());
        String lowerMessage = message != null ? message.toLowerCase() : "";

        LOG.error("Database constraint violation while trying to {} VisitDuration: {}", operation, message, constraintException);

        if (lowerMessage.contains("ux_visit_duration_type_duration_global") ||
                lowerMessage.contains("unique constraint") ||
                lowerMessage.contains("duplicate key") ||
                lowerMessage.contains("duplicate entry")) {

            throw new BadRequestAlertException(
                    "A global visit duration with the same visit type and duration already exists (resource_specific = false).",
                    "visitDuration",
                    "unique.visitDuration.global"
            );
        }

        throw new BadRequestAlertException(
                "Database constraint violated while trying to " + operation +
                        " visit duration (check unique visit type + duration or required fields).",
                "visitDuration",
                "db.constraint"
        );
    }
}
