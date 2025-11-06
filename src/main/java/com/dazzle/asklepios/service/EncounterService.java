package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Encounter;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.enumeration.Resource;
import com.dazzle.asklepios.domain.enumeration.Status;
import com.dazzle.asklepios.repository.EncounterRepository;
import com.dazzle.asklepios.repository.PatientRepository;
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
import jakarta.persistence.EntityManager;
import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
public class EncounterService {
    private static final Logger LOG = LoggerFactory.getLogger(EncounterService.class);
    private final EncounterRepository encounterRepository;
    private final PatientRepository patientRepository;
    private final EntityManager em;

    public EncounterService(EncounterRepository encounterRepository, PatientRepository patientRepository, EntityManager em) {
        this.encounterRepository = encounterRepository;
        this.patientRepository = patientRepository;
        this.em = em;
    }

    public Encounter create(Encounter encounter,Long patientId) {
        LOG.debug("Request to create Encounter : {}", encounter);

        if (patientId == null) {
            throw new BadRequestAlertException("patient ID is required", "patientId", "patient.required");
        }
        if (encounter == null) {
            throw new BadRequestAlertException("encounter is required", "encounter", "encounter.required");
        }

        Patient patient = patientRepository.findById(patientId).orElseThrow(()-> new NotFoundAlertException("patient not found with id"+ patientId,"patient","notfound"));

        Encounter encounterToSave = Encounter.builder()
                .age(encounter.getAge())
                .status(encounter.getStatus())
                .resourceType(encounter.getResourceType())
                .visitType(encounter.getVisitType())
                .patient(refPatient(patientId))
                .build();
        try{
           Encounter saved = encounterRepository.saveAndFlush(encounterToSave);
           LOG.info("Successfully Created Encounter id={} status='{}' for patient={}",saved.getId(),saved.getStatus(),patientId);
           return saved;
        }catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage()).toLowerCase();

            LOG.error("Database constraint violation while creating Encounter: {}", message, ex);

            if (message.contains("fk_patient_encounter") ||
                    message.contains("unique constraint") ||
                    message.contains("duplicate key") ||
                    message.contains("duplicate entry")) {
                throw new BadRequestAlertException(
                        "A Encounter with the same name already exists in this facility.",
                        "Encounter",
                        "unique.patient.name"
                );
            }
            throw new BadRequestAlertException(
                    "Database constraint violated while creating Encounter (check facility, unique name, or required fields).",
                    "Encounter",
                    "db.constraint"
            );
}

}

    public Optional<Encounter> update(Long id,Long patientId, Encounter patch) {
        LOG.debug("Request to update Encounter id={} with {}", id, patch);

        if (patch == null){
            throw new BadRequestAlertException("Encounter payload is required", "Encounter", "encounter.required");
        }

        Encounter existing = encounterRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Encounter not found with id " + id, "encounter", "notfound"));
        existing.setAge(patch.getAge());
        existing.setStatus(patch.getStatus());
        existing.setResourceType(patch.getResourceType());
        existing.setVisitType(patch.getVisitType());
        try{
            Encounter updated = encounterRepository.saveAndFlush(existing);
            LOG.info("Successfully updated encounter id={} (age='{}')", updated.getId(), updated.getAge());
            return Optional.of(updated);
        }catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage()).toLowerCase();

            LOG.error("Database constraint violation while creating service: {}", message, ex);
            if (message.contains("fk_patient_encounter") ||
                    message.contains("unique constraint") ||
                    message.contains("duplicate key") ||
                    message.contains("duplicate entry")) {
                throw new BadRequestAlertException(
                        "A Encounter with the same name already exists in this facility.",
                        "Encounter",
                        "unique.patient.name"
                );
            }
        }
        throw new BadRequestAlertException(
                "Database constraint violated while creating Encounter (check facility, unique name, or required fields).",
                "Encounter",
                "db.constraint"
        );
        }

    public void delete(Long id) {
        LOG.debug("Request to delete Encounter id={}", id);
        encounterRepository.deleteById(id);
    }

    /* ============================= Queries ============================= */

    @Transactional(readOnly = true)
    public Optional<Encounter> findOne(Long id) {
        LOG.debug("Request to get Encounter : {}", id);
        return encounterRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Page<Encounter> findAll(Pageable pageable) {
        LOG.debug("Request to get all Encounters");
        return encounterRepository.findAll(pageable);
    }
    @Transactional(readOnly = true)
    public Page<Encounter> findByStatus(Status status, Pageable pageable) {
        LOG.debug("Request to get Encounters by status={}", status);
        return encounterRepository.findByStatus(status,pageable);
    }

    @Transactional(readOnly = true)
    public Page<Encounter> findByPatientId(Long patientId,Pageable pageable) {
        LOG.debug("Request to get Encounters by patientId={}", patientId);
        return encounterRepository.findByPatient_Id(patientId,pageable);
    }

    @Transactional(readOnly = true)
    public Page<Encounter> findByResource(Resource resource,Pageable pageable) {
        LOG.debug("Request to get Encounters by resource={}", resource);
        return encounterRepository.findByResourceType(resource,pageable);
    }

    // ====================== Helpers ======================
    private Patient refPatient(Long patientId) {
        return em.getReference(Patient.class, patientId);
    }
}
