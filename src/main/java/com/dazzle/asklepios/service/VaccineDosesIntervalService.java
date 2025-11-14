package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Vaccine;
import com.dazzle.asklepios.domain.VaccineDoses;
import com.dazzle.asklepios.domain.VaccineDosesInterval;
import com.dazzle.asklepios.repository.VaccineDosesIntervalRepository;
import com.dazzle.asklepios.repository.VaccineDosesRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class VaccineDosesIntervalService {

    private static final Logger LOG = LoggerFactory.getLogger(VaccineDosesIntervalService.class);

    private final VaccineDosesIntervalRepository vaccineDosesIntervalRepository;
    private final EntityManager entityManager;
    private final VaccineDosesRepository vaccineDosesRepository;
    public VaccineDosesIntervalService(
            VaccineDosesIntervalRepository vaccineDosesIntervalRepository,
            EntityManager entityManager, VaccineDosesRepository vaccineDosesRepository
    ) {
        this.vaccineDosesIntervalRepository = vaccineDosesIntervalRepository;
        this.entityManager = entityManager;
        this.vaccineDosesRepository = vaccineDosesRepository;
    }

    // ====================== CREATE ======================
    public VaccineDosesInterval create(Long vaccineId, VaccineDosesInterval incoming) {
        LOG.info("[CREATE] Request to create VaccineDosesInterval for vaccineId={} payload={}", vaccineId, incoming);

        if (vaccineId == null) {
            throw new BadRequestAlertException("Vaccine id is required", "vaccineDosesInterval", "vaccine.required");
        }
        if (incoming == null) {
            throw new BadRequestAlertException("Vaccine doses interval payload is required", "vaccineDosesInterval", "payload.required");
        }

        VaccineDosesInterval entity = VaccineDosesInterval.builder()
                .vaccine(refVaccine(vaccineId))
                .fromDose(refDose(incoming.getFromDose().getId()))
                .toDose(refDose(incoming.getToDose().getId()))
                .intervalBetweenDoses(incoming.getIntervalBetweenDoses())
                .isActive(incoming.getIsActive())
                .unit(incoming.getUnit())
                .build();

        try {
            VaccineDosesInterval saved = vaccineDosesIntervalRepository.saveAndFlush(entity);
            LOG.info("Successfully created vaccine doses interval id={} for vaccineId={} fromDoseId={} toDoseId={}",
                    saved.getId(), vaccineId, entity.getFromDose().getId(), entity.getToDose().getId());
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            handleConstraintViolation(constraintException);
            throw constraintException;
        }
    }

    public Optional<VaccineDosesInterval> update(Long id, Long vaccineId, VaccineDosesInterval incoming) {
        LOG.info("[UPDATE] Request to update VaccineDosesInterval id={} vaccineId={} payload={}", id, vaccineId, incoming);

        if (incoming == null) {
            throw new BadRequestAlertException("Vaccine doses interval payload is required", "vaccineDosesInterval", "payload.required");
        }

        VaccineDosesInterval existing = vaccineDosesIntervalRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Vaccine doses interval not found with id " + id, "vaccineDosesInterval", "notfound"));

        existing.setFromDose(refDose(incoming.getFromDose().getId()));
        existing.setToDose(refDose(incoming.getToDose().getId()));
        existing.setIntervalBetweenDoses(incoming.getIntervalBetweenDoses());
        existing.setUnit(incoming.getUnit());
        existing.setIsActive(incoming.getIsActive());

        try {
            VaccineDosesInterval updated = vaccineDosesIntervalRepository.saveAndFlush(existing);
            LOG.info("Successfully updated vaccine doses interval id={} (fromDoseId={}, toDoseId={})",
                    updated.getId(),
                    updated.getFromDose() != null ? updated.getFromDose().getId() : null,
                    updated.getToDose() != null ? updated.getToDose().getId() : null);
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            handleConstraintViolation(ex);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public Page<VaccineDosesInterval> findByVaccineId(Long vaccineId, Pageable pageable) {
        LOG.debug("Fetching VaccineDosesInterval by vaccineId={} pageable={}", vaccineId, pageable);
        return vaccineDosesIntervalRepository.findByVaccine_Id(vaccineId, pageable);
    }

    public Optional<VaccineDosesInterval> toggleIsActive(Long id) {
        LOG.info("Toggling isActive for VaccineDosesInterval id={}", id);
        return vaccineDosesIntervalRepository.findById(id)
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    VaccineDosesInterval saved = vaccineDosesIntervalRepository.save(entity);
                    LOG.info("VaccineDosesInterval id={} active status changed to {}", id, saved.getIsActive());
                    return saved;
                });
    }
    private Vaccine refVaccine(Long vaccineId) {
        return entityManager.getReference(Vaccine.class, vaccineId);
    }

    private VaccineDoses refDose(Long doseId) {
        return entityManager.getReference(VaccineDoses.class, doseId);
    }

    private void handleConstraintViolation(RuntimeException constraintException) {
        Throwable root = getRootCause(constraintException);
        String message = (root != null ? root.getMessage() : constraintException.getMessage());
        String msgLower = message != null ? message.toLowerCase() : "";

        LOG.error("Database constraint violation while saving vaccine doses interval: {}", message, constraintException);

        if (msgLower.contains("ux_vdi_vaccine_from_to")
                || msgLower.contains("unique constraint")
                || msgLower.contains("duplicate key")
                || msgLower.contains("duplicate entry")) {

            throw new BadRequestAlertException(
                    "A doses-interval with the same (vaccine, from_dose, to_dose) already exists.",
                    "vaccineDosesInterval",
                    "unique.vaccine.from.to"
            );
        }

        if (msgLower.contains("ck_vdi_positive_interval")) {
            throw new BadRequestAlertException(
                    "interval_between_doses must be greater than 0.",
                    "vaccineDosesInterval",
                    "interval.positive"
            );
        }

        if (msgLower.contains("ck_vdi_from_ne_to")) {
            throw new BadRequestAlertException(
                    "from_dose_id and to_dose_id must be different.",
                    "vaccineDosesInterval",
                    "from.ne.to"
            );
        }

        if (msgLower.contains("fk_vdi_from_dose") || msgLower.contains("fk_vdi_to_dose")) {
            throw new BadRequestAlertException(
                    "from_dose_id and to_dose_id must reference existing vaccine doses.",
                    "vaccineDosesInterval",
                    "dose.fk.notfound"
            );
        }

        throw new BadRequestAlertException(
                "Database constraint violated while saving vaccine doses interval (check unique fields or required values).",
                "vaccineDosesInterval",
                "db.constraint"
        );
    }


    @Transactional(readOnly = true)
    public List<VaccineDoses> findDosesByVaccineExcludingFirst(Long vaccineId, Long fromDoseId) {
        LOG.debug("Fetching doses for vaccineId={}, after doseId={}", vaccineId, fromDoseId);

        if (vaccineId == null) {
            throw new BadRequestAlertException("vaccineId is required", "vaccineDose", "vaccine.required");
        }
        if (fromDoseId == null) {
            throw new BadRequestAlertException("fromDoseId is required", "vaccineDose", "fromDose.required");
        }

        List<VaccineDoses> doses = vaccineDosesRepository.findByVaccine_Id(vaccineId);
        VaccineDoses fromDose = doses.stream()
                .filter(dose -> fromDoseId.equals(dose.getId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestAlertException(
                        "fromDoseId not found for given vaccineId",
                        "vaccineDose",
                        "fromDose.notFound")
                );

        Integer fromOrder = fromDose.getDoseNumber().getOrder();

        Integer maxOrder = doses.stream()
                .mapToInt(d -> d.getDoseNumber().getOrder())
                .max()
                .orElse(fromOrder);

        if (fromOrder >= maxOrder) {
            return List.of();
        }

        return doses.stream()
                .filter(dose -> dose.getDoseNumber().getOrder() > fromOrder)
                .sorted(Comparator.comparingInt(dose -> dose.getDoseNumber().getOrder()))
                .toList();
    }


}
