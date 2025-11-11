package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Vaccine;
import com.dazzle.asklepios.domain.VaccineDoses;
import com.dazzle.asklepios.domain.enumeration.DoseNumber;
import com.dazzle.asklepios.domain.enumeration.NumberOfDoses;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class VaccineDosesService {

    private static final Logger LOG = LoggerFactory.getLogger(VaccineDosesService.class);

    private final VaccineDosesRepository vaccineDosesRepository;
    private final EntityManager entityManager;

    public VaccineDosesService(VaccineDosesRepository vaccineDosesRepository, EntityManager entityManager) {
        this.vaccineDosesRepository = vaccineDosesRepository;
        this.entityManager = entityManager;
    }

    public VaccineDoses create(Long vaccineId, VaccineDoses incoming) {
        LOG.info("[CREATE] Request to create VaccineDose for vaccineId={} payload={}", vaccineId, incoming);

        if (vaccineId == null) {
            throw new BadRequestAlertException("Vaccine id is required", "vaccineDose", "vaccine.required");
        }
        if (incoming == null) {
            throw new BadRequestAlertException("Vaccine dose payload is required", "vaccineDose", "payload.required");
        }

        VaccineDoses entity = VaccineDoses.builder()
                .vaccine(refVaccine(vaccineId))
                .doseNumber(incoming.getDoseNumber())
                .fromAge(incoming.getFromAge())
                .toAge(incoming.getToAge())
                .fromAgeUnit(incoming.getFromAgeUnit())
                .toAgeUnit(incoming.getToAgeUnit())
                .isBooster(Boolean.TRUE.equals(incoming.getIsBooster()))
                .isActive(Boolean.TRUE.equals(incoming.getIsActive()))
                .build();

        try {
            VaccineDoses saved = vaccineDosesRepository.saveAndFlush(entity);
            LOG.info("Successfully created vaccine dose id={} doseNumber={} for vaccineId={}", saved.getId(), saved.getDoseNumber(), vaccineId);
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            handleConstraintViolation(constraintException);
            throw constraintException;
        }
    }

    public Optional<VaccineDoses> update(Long id, Long vaccineId, VaccineDoses incoming) {
        LOG.info("[UPDATE] Request to update VaccineDose id={} vaccineId={} payload={}", id, vaccineId, incoming);

        if (incoming == null) {
            throw new BadRequestAlertException("Vaccine dose payload is required", "vaccineDose", "payload.required");
        }

        VaccineDoses existing = vaccineDosesRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Vaccine dose not found with id " + id, "vaccineDose", "notfound"));

        existing.setDoseNumber(incoming.getDoseNumber());
        existing.setFromAge(incoming.getFromAge());
        existing.setToAge(incoming.getToAge());
        existing.setFromAgeUnit(incoming.getFromAgeUnit());
        existing.setToAgeUnit(incoming.getToAgeUnit());
        existing.setIsBooster(incoming.getIsBooster());
        existing.setIsActive(incoming.getIsActive());
        try {
            VaccineDoses updated = vaccineDosesRepository.saveAndFlush(existing);
            LOG.info("Successfully updated vaccine dose id={} (doseNumber={})", updated.getId(), updated.getDoseNumber());
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            handleConstraintViolation(ex);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public Page<VaccineDoses> findByVaccineId(Long vaccineId, Pageable pageable) {
        LOG.debug("Fetching VaccineDoses by vaccineId={} pageable={}", vaccineId, pageable);
        return vaccineDosesRepository.findByVaccine_Id(vaccineId, pageable);
    }

    public Optional<VaccineDoses> toggleIsActive(Long id) {
        LOG.info("Toggling isActive for VaccineDose id={}", id);
        return vaccineDosesRepository.findById(id)
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    VaccineDoses saved = vaccineDosesRepository.save(entity);
                    LOG.info("VaccineDose id={} active status changed to {}", id, saved.getIsActive());
                    return saved;
                });
    }

    private Vaccine refVaccine(Long vaccineId) {
        return entityManager.getReference(Vaccine.class, vaccineId);
    }

    private void handleConstraintViolation(RuntimeException constraintException) {
        Throwable root = getRootCause(constraintException);
        String message = (root != null ? root.getMessage() : constraintException.getMessage());
        String msgLower = message != null ? message.toLowerCase() : "";

        LOG.error("Database constraint violation while saving vaccine dose: {}", message, constraintException);

        // Match your expected unique constraint names; keep generic dup checks too.
        if (msgLower.contains("ux_vaccine_dose_number_vaccine_id")
                || msgLower.contains("ux_vaccine_dose_full_age_window")
                || msgLower.contains("unique constraint")
                || msgLower.contains("duplicate key")
                || msgLower.contains("duplicate entry")) {

            if (msgLower.contains("ux_vaccine_dose_number_vaccine_id")) {
                throw new BadRequestAlertException(
                        "A vaccine dose with the same (dose_number, vaccine_id) already exists.",
                        "vaccineDose",
                        "unique.doseNumber.vaccineId"
                );
            }
            if (msgLower.contains("ux_vaccine_dose_full_age_window")) {
                throw new BadRequestAlertException(
                        "A vaccine dose with the same (dose_number, vaccine_id, from/to age + units) already exists.",
                        "vaccineDose",
                        "unique.doseNumber.vaccineId.ageWindow"
                );
            }

            throw new BadRequestAlertException(
                    "Duplicate or unique constraint violation while saving vaccine dose.",
                    "vaccineDose",
                    "db.duplicate"
            );
        }

        throw new BadRequestAlertException(
                "Database constraint violated while saving vaccine dose (check unique fields or required values).",
                "vaccineDose",
                "db.constraint"
        );
    }
    @Transactional(readOnly = true)
    public List<DoseNumber> getDoseNumbersUpTo(NumberOfDoses numberOfDoses) {
        LOG.debug("Listing DoseNumber values up to {}", numberOfDoses);

        if (numberOfDoses == null) {
            throw new BadRequestAlertException("NumberOfDoses is required", "vaccineDose", "numberOfDoses.required");
        }

        final Integer count = numberOfDoses.getValue();
        if (count == null || count <= 0) {
            throw new BadRequestAlertException("Invalid NumberOfDoses value", "vaccineDose", "numberOfDoses.invalid");
        }

        return Arrays.stream(DoseNumber.values())
                .sorted(Comparator.comparingInt(DoseNumber::getOrder))
                .limit(count)
                .collect(Collectors.toList());
    }

}
