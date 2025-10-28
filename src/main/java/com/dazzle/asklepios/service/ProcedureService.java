package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.enumeration.ProcedureCategoryType;
import com.dazzle.asklepios.repository.ProcedureRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class ProcedureService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcedureService.class);

    private final ProcedureRepository procedureRepository;
    private final EntityManager em;

    public ProcedureService(ProcedureRepository procedureRepository, EntityManager em) {
        this.procedureRepository = procedureRepository;
        this.em = em;
    }

    // ====================== CREATE ======================
    public Procedure create(Long facilityId, Procedure incoming) {
        LOG.info("[CREATE] Request to create Procedure for facilityId={} payload={}", facilityId, incoming);

        if (facilityId == null) {
            throw new BadRequestAlertException("Facility id is required", "procedure", "facility.required");
        }
        if (incoming == null) {
            throw new BadRequestAlertException("Procedure payload is required", "procedure", "payload.required");
        }

        Procedure entity = Procedure.builder()
                .name(incoming.getName())
                .code(incoming.getCode())
                .categoryType(incoming.getCategoryType())
                .isAppointable(Boolean.TRUE.equals(incoming.getIsAppointable()))
                .indications(incoming.getIndications())
                .contraindications(incoming.getContraindications())
                .preparationInstructions(incoming.getPreparationInstructions())
                .recoveryNotes(incoming.getRecoveryNotes())
                .isActive(Boolean.TRUE.equals(incoming.getIsActive()))
                .facility(refFacility(facilityId))
                .build();

        try {
            Procedure saved = procedureRepository.saveAndFlush(entity);
            LOG.info("Successfully created procedure id={} name='{}' for facilityId={}", saved.getId(), saved.getName(), facilityId);
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage());
            String msgLower = message != null ? message.toLowerCase() : "";

            LOG.error("Database constraint violation while creating procedure: {}", message, ex);

            if (msgLower.contains("uk_procedure_facility_name_code_categorytype")
                    || msgLower.contains("unique constraint")
                    || msgLower.contains("duplicate key")
                    || msgLower.contains("duplicate entry")) {
                throw new BadRequestAlertException(
                        "A procedure with the same name, code, and category already exists in this facility.",
                        "procedure",
                        "unique.facility.name_code_category"
                );
            }
            throw new BadRequestAlertException(
                    "Database constraint violated while creating procedure (check facility, unique fields, or required values).",
                    "procedure",
                    "db.constraint"
            );
        }
    }

    // ====================== UPDATE ======================
    public Optional<Procedure> update(Long id, Long facilityId, Procedure incoming) {
        LOG.info("[UPDATE] Request to update Procedure id={} facilityId={} payload={}", id, facilityId, incoming);

        if (incoming == null) {
            throw new BadRequestAlertException("Procedure payload is required", "procedure", "payload.required");
        }

        Procedure existing = procedureRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Procedure not found with id " + id, "procedure", "notfound"));

        if (facilityId != null && (existing.getFacility() == null || !facilityId.equals(existing.getFacility().getId()))) {
            throw new BadRequestAlertException("Procedure does not belong to the given facility.", "procedure", "facility.mismatch");
        }

        existing.setName(incoming.getName());
        existing.setCode(incoming.getCode());
        existing.setCategoryType(incoming.getCategoryType());
        existing.setIsAppointable(incoming.getIsAppointable());
        existing.setIndications(incoming.getIndications());
        existing.setContraindications(incoming.getContraindications());
        existing.setPreparationInstructions(incoming.getPreparationInstructions());
        existing.setRecoveryNotes(incoming.getRecoveryNotes());
        existing.setIsActive(incoming.getIsActive());

        try {
            Procedure updated = procedureRepository.saveAndFlush(existing);
            LOG.info("Successfully updated procedure id={} (name='{}')", updated.getId(), updated.getName());
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage());
            String msgLower = message != null ? message.toLowerCase() : "";

            LOG.error("Database constraint violation while updating procedure: {}", message, ex);

            if (msgLower.contains("uk_procedure_facility_name_code_categorytype")
                    || msgLower.contains("unique constraint")
                    || msgLower.contains("duplicate key")
                    || msgLower.contains("duplicate entry")) {
                throw new BadRequestAlertException(
                        "A procedure with the same name, code, and category already exists in this facility.",
                        "procedure",
                        "unique.facility.name_code_category"
                );
            }
            throw new BadRequestAlertException(
                    "Database constraint violated while updating procedure (check facility, unique fields, or required values).",
                    "procedure",
                    "db.constraint"
            );
        }
    }

    // ====================== READ ======================
    @Transactional(readOnly = true)
    public List<Procedure> findAll(Long facilityId) {
        LOG.debug("Fetching all Procedures for facilityId={}", facilityId);
        return procedureRepository.findByFacility_Id(facilityId);
    }

    @Transactional(readOnly = true)
    public Page<Procedure> findAll(Long facilityId, Pageable pageable) {
        LOG.debug("Fetching paged Procedures for facilityId={} pageable={}", facilityId, pageable);
        return procedureRepository.findByFacility_Id(facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Procedure> findByCategory(Long facilityId, ProcedureCategoryType categoryType, Pageable pageable) {
        LOG.debug("Fetching Procedures by categoryType={} facilityId={}", categoryType, facilityId);
        return procedureRepository.findByFacility_IdAndCategoryType(facilityId, categoryType, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Procedure> findByCodeContainingIgnoreCase(Long facilityId, String code, Pageable pageable) {
        LOG.debug("Fetching Procedures by code='{}' facilityId={}", code, facilityId);
        return procedureRepository.findByFacility_IdAndCodeContainingIgnoreCase(facilityId, code, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Procedure> findByNameContainingIgnoreCase(Long facilityId, String name, Pageable pageable) {
        LOG.debug("Fetching Procedures by name='{}' facilityId={}", name, facilityId);
        return procedureRepository.findByFacility_IdAndNameContainingIgnoreCase(facilityId, name, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Procedure> findOne(Long id) {
        LOG.debug("Fetching single Procedure by id={}", id);
        return procedureRepository.findById(id);
    }

    // ====================== TOGGLE ======================
    public Optional<Procedure> toggleIsActive(Long id, Long facilityId) {
        LOG.info("Toggling isActive for Procedure id={} facilityId={}", id, facilityId);
        return procedureRepository.findById(id)
                .filter(proc -> proc.getFacility() != null && proc.getFacility().getId().equals(facilityId))
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    Procedure saved = procedureRepository.save(entity);
                    LOG.info("Procedure id={} active status changed to {}", id, saved.getIsActive());
                    return saved;
                });
    }

    // ====================== Helpers ======================
    private Facility refFacility(Long facilityId) {
        return em.getReference(Facility.class, facilityId);
    }
}
