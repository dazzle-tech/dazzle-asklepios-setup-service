package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient create(Patient incoming) {
        LOG.debug("Request to create Patient : {}", incoming);

        if (incoming == null) {
            throw new BadRequestAlertException("Patient payload is required", "patient", "patient.required");
        }
        if (incoming.getEmail() == null || incoming.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (patientRepository.existsByEmail(incoming.getEmail())) {
            throw new BadRequestAlertException("Email already in use: " + incoming.getEmail(), "patient", "emailexists");
        }
        Patient patientToSave = Patient.builder()
                .firstName(incoming.getFirstName())
                .lastName(incoming.getLastName())
                .email(incoming.getEmail())
                .dateOfBirth(incoming.getDateOfBirth())
                .gender(incoming.getGender())
                    .build();
        try {
            Patient saved = patientRepository.saveAndFlush(patientToSave);
            LOG.info("Successfully created Patient id={} email='{}'", saved.getId(), saved.getEmail());
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage());
            String msg = (message == null ? "" : message).toLowerCase();

            LOG.error("DB constraint violation while creating patient: {}", message, ex);

            if (msg.contains("uk_encounter_patient_visit") ||
                    msg.contains("unique") ||
                    msg.contains("duplicate key") ||
                    msg.contains("duplicate entry")) {

                throw new BadRequestAlertException(
                        "Email already in use: " + incoming.getEmail(),
                        "patient",
                        "emailexists"
                );
            }

            throw new BadRequestAlertException(
                    "Database constraint violated while creating patient.",
                    "patient",
                    "db.constraint"
            );
        }
    }

    public Optional<Patient> update(Long id, Patient patch) {
        LOG.debug("Request to update Patient id={} with {}", id, patch);

        if (patch == null) {
            throw new BadRequestAlertException("Patient payload is required", "patient", "patient.required");
        }

        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Patient not found with id " + id, "patient", "notfound"));
        existing.setFirstName(patch.getFirstName());
        existing.setLastName(patch.getLastName());
        existing.setEmail(patch.getEmail());
        existing.setDateOfBirth(patch.getDateOfBirth());
        existing.setGender(patch.getGender());
        try {
            Patient updated = patientRepository.saveAndFlush(existing);
            LOG.info("Successfully updated patient id={} (email='{}')", updated.getId(), updated.getEmail());
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage());
            String msg = (message == null ? "" : message).toLowerCase();

            LOG.error("Database constraint violation while updating patient id={}: {}", id, message, ex);

            if (msg.contains("uk_patient_email")
                    || msg.contains("unique")
                    || msg.contains("duplicate key")
                    || msg.contains("duplicate entry")) {
                throw new BadRequestAlertException(
                        "Email already in use: " + (patch.getEmail() != null ? patch.getEmail() : existing.getEmail()),
                        "patient",
                        "emailexists"
                );
            }

            throw new BadRequestAlertException(
                    "Database constraint violated while updating patient.",
                    "patient",
                    "db.constraint"
            );
        }
    }

    /** حذف */
    public void delete(Long id) {
        LOG.debug("Request to delete Patient id={}", id);
        patientRepository.deleteById(id);
    }

    /** جلب واحد */
    @Transactional(readOnly = true)
    public Optional<Patient> findOne(Long id) {
        LOG.debug("Request to get Patient : {}", id);
        return patientRepository.findById(id);
    }

    /** جميع المرضى بدون ترقيم */
    @Transactional(readOnly = true)
    public List<Patient> findAll() {
        LOG.debug("Request to get all Patients (no pagination)");
        return patientRepository.findAll();
    }

    /** بحسب الإيميل */
    @Transactional(readOnly = true)
    public Optional<Patient> findByEmail(String email) {
        LOG.debug("Request to get Patient by email : {}", email);
        return patientRepository.findByEmail(email);
    }
}
