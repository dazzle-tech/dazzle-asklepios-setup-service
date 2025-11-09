package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.AgeGroup;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import com.dazzle.asklepios.repository.AgeGroupRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class AgeGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(AgeGroupService.class);
    private final AgeGroupRepository ageGroupRepository;
    private final EntityManager entityManager;

    public AgeGroupService(AgeGroupRepository ageGroupRepository, EntityManager entityManager) {
        this.ageGroupRepository = ageGroupRepository;
        this.entityManager = entityManager;
    }

    public AgeGroup create(Long facilityId, AgeGroup incoming) {
        LOG.info("[CREATE] Request to create AgeGroup for facilityId={} payload={}", facilityId, incoming);
        if (facilityId == null) {
            throw new BadRequestAlertException("Facility id is required", "ageGroup", "facility.required");
        }
        if (incoming == null) {
            throw new BadRequestAlertException("AgeGroup payload is required", "ageGroup", "payload.required");
        }
        AgeGroup entity = AgeGroup.builder()
                .ageGroup(incoming.getAgeGroup())
                .fromAge(incoming.getFromAge())
                .toAge(incoming.getToAge())
                .fromAgeUnit(incoming.getFromAgeUnit())
                .toAgeUnit(incoming.getToAgeUnit())
                .facility(refFacility(facilityId))
                .build();
        try {
            AgeGroup saved = ageGroupRepository.saveAndFlush(entity);
            LOG.info("Successfully created AgeGroup id={} label='{}' for facilityId={}", saved.getId(), saved.getAgeGroup(), facilityId);
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage()).toLowerCase();
            LOG.error("Database constraint violation while creating AgeGroup: {}", message, ex);
            if (message.contains("uq_facility_age_range") ||
                    message.contains("uq_facility_age_group_label") ||
                    message.contains("unique constraint") ||
                    message.contains("duplicate key") ||
                    message.contains("duplicate entry")) {
                throw new BadRequestAlertException(
                        "An age group with the same name or age range already exists in this facility.",
                        "ageGroup",
                        "unique.facility.ageGroup"
                );
            }
            throw new BadRequestAlertException(
                    "Database constraint violated while creating age group (check facility, unique name, or required fields).",
                    "ageGroup",
                    "db.constraint"
            );
        }
    }

    public Optional<AgeGroup> update(Long id, Long facilityId, AgeGroup incoming) {
        LOG.info("[UPDATE] Request to update AgeGroup id={} facilityId={} payload={}", id, facilityId, incoming);
        if (incoming == null) {
            throw new BadRequestAlertException("AgeGroup payload is required", "ageGroup", "payload.required");
        }
        AgeGroup existing = ageGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("AgeGroup not found with id " + id, "ageGroup", "notfound"));
        if (facilityId != null && (existing.getFacility() == null || !facilityId.equals(existing.getFacility().getId()))) {
            throw new BadRequestAlertException("AgeGroup does not belong to the provided facility", "ageGroup", "facility.mismatch");
        }
        existing.setAgeGroup(incoming.getAgeGroup());
        existing.setFromAge(incoming.getFromAge());
        existing.setToAge(incoming.getToAge());
        existing.setFromAgeUnit(incoming.getFromAgeUnit());
        existing.setToAgeUnit(incoming.getToAgeUnit());
        try {
            AgeGroup updated = ageGroupRepository.saveAndFlush(existing);
            LOG.info("Successfully updated AgeGroup id={} (label='{}')", updated.getId(), updated.getAgeGroup());
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage()).toLowerCase();
            LOG.error("Database constraint violation while updating AgeGroup: {}", message, ex);
            if (message.contains("uq_facility_age_range") ||
                    message.contains("uq_facility_age_group_label") ||
                    message.contains("unique constraint") ||
                    message.contains("duplicate key") ||
                    message.contains("duplicate entry")) {
                throw new BadRequestAlertException(
                        "An age group with the same name or age range already exists in this facility.",
                        "ageGroup",
                        "unique.facility.ageGroup"
                );
            }
            throw new BadRequestAlertException(
                    "Database constraint violated while updating age group (check facility, unique name, or required fields).",
                    "ageGroup",
                    "db.constraint"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<AgeGroup> findAll(Long facilityId) {
        LOG.debug("Fetching all AgeGroups for facilityId={}", facilityId);
        return ageGroupRepository.findByFacility_Id(facilityId);
    }

    @Transactional(readOnly = true)
    public Page<AgeGroup> findAll(Long facilityId, Pageable pageable) {
        LOG.debug("Fetching paged AgeGroups for facilityId={} pageable={}", facilityId, pageable);
        return ageGroupRepository.findByFacility_Id(facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AgeGroup> findByAgeGroup(Long facilityId, AgeGroupType label, Pageable pageable) {
        LOG.debug("Fetching AgeGroups by label='{}' facilityId={}", label, facilityId);
        return ageGroupRepository.findByFacility_IdAndAgeGroup(facilityId, label, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AgeGroup> findByFromAge(Long facilityId, BigDecimal fromAge, Pageable pageable) {
        LOG.debug("Fetching AgeGroups by fromAge='{}' facilityId={}", fromAge, facilityId);
        return ageGroupRepository.findByFacility_IdAndFromAge(facilityId, fromAge, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AgeGroup> findByToAge(Long facilityId, BigDecimal toAge, Pageable pageable) {
        LOG.debug("Fetching AgeGroups by toAge='{}' facilityId={}", toAge, facilityId);
        return ageGroupRepository.findByFacility_IdAndToAge(facilityId, toAge, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<AgeGroup> findOne(Long id) {
        LOG.debug("Fetching single AgeGroup by id={}", id);
        return ageGroupRepository.findById(id);
    }

    public boolean delete(Long id) {
        LOG.debug("Request to delete AgeGroup : {}", id);

        if (id == null) {
            LOG.warn("Delete request failed — AgeGroup id is null");
            return false;
        }

        if (!ageGroupRepository.existsById(id)) {
            LOG.warn("Delete request failed — AgeGroup not found with id={}", id);
            return false;
        }

        try {
            ageGroupRepository.deleteById(id);
            LOG.info("Successfully deleted AgeGroup with id={}", id);
            return true;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage());
            LOG.error("Database constraint violation while deleting AgeGroup id={}: {}", id, message, ex);
            return false;
        } catch (Exception ex) {
            LOG.error("Unexpected error occurred while deleting AgeGroup id={}: {}", id, ex.getMessage(), ex);
            return false;
        }
    }

    private Facility refFacility(Long facilityId) {
        return entityManager.getReference(Facility.class, facilityId);
    }
}
