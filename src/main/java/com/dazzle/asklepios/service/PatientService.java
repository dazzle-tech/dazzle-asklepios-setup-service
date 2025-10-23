package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientService.class);
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient create(Patient patient) {
        LOG.debug("Request to create Patient : {}", patient);

        if (patient.getEmail() == null || patient.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (patientRepository.existsByEmail(patient.getEmail())) {
            throw new BadRequestAlertException("Email already in use: " + patient.getEmail(), "patient", "emailexists");
        }

        try {
            return patientRepository.saveAndFlush(patient);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage());
            String msg = (message == null ? "" : message).toLowerCase();

            LOG.error("DB constraint violation while creating patient: {}", message, ex);

            if (msg.contains("uk_encounter_patient_visit") ||        // اسم القيد اللي أضفته على patient.email
                    msg.contains("unique") ||
                    msg.contains("duplicate key") ||
                    msg.contains("duplicate entry")) {

                throw new BadRequestAlertException(
                        "Email already in use: " + patient.getEmail(),
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

    /** تحديث جزئي/كامل بناءً على الحقول غير-null */
    public Optional<Patient> update(Long id, Patient patch) {
        LOG.debug("Request to update Patient id={} with {}", id, patch);

        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Patient not found with id " + id, "patient", "notfound"));

        if (patch.getEmail() != null && !patch.getEmail().equalsIgnoreCase(existing.getEmail())) {
            if (patientRepository.existsByEmail(patch.getEmail())) {
                throw new BadRequestAlertException("Email already in use: " + patch.getEmail(), "patient", "emailexists");
            }
            existing.setEmail(patch.getEmail());
        }

        if (patch.getFirstName()     != null) existing.setFirstName(patch.getFirstName());
        if (patch.getLastName()      != null) existing.setLastName(patch.getLastName());
        if (patch.getDateOfBirth()   != null) existing.setDateOfBirth(patch.getDateOfBirth());
        if (patch.getGender()        != null) existing.setGender(patch.getGender());
        if (patch.getLastModifiedBy()!= null) existing.setLastModifiedBy(patch.getLastModifiedBy());

        try {
            return Optional.of(patientRepository.saveAndFlush(existing));
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage());
            String msg = (message == null ? "" : message).toLowerCase();

            LOG.error("DB constraint violation while updating patient id={}: {}", id, message, ex);

            if (msg.contains("uk_encounter_patient_visit") ||
                    msg.contains("unique") ||
                    msg.contains("duplicate key") ||
                    msg.contains("duplicate entry")) {

                throw new BadRequestAlertException(
                        "Email already in use: " + existing.getEmail(),
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

    /* ================= Helpers ================= */

    private static Throwable getRootCause(Throwable t) {
        Throwable result = t;
        while (result != null && result.getCause() != null && result.getCause() != result) {
            result = result.getCause();
        }
        return result;
    }
}
