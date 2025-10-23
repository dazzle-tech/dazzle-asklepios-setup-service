package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Encounter;
import com.dazzle.asklepios.domain.enumeration.Resource;
import com.dazzle.asklepios.domain.enumeration.Status;
import com.dazzle.asklepios.domain.enumeration.Visit;
import com.dazzle.asklepios.repository.EncounterRepository;
import com.dazzle.asklepios.repository.PatientRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EncounterService {

    private static final Logger LOG = LoggerFactory.getLogger(EncounterService.class);

    private final EncounterRepository encounterRepository;
    private final PatientRepository patientRepository;

    public EncounterService(EncounterRepository encounterRepository, PatientRepository patientRepository) {
        this.encounterRepository = encounterRepository;
        this.patientRepository = patientRepository;
    }

    public Encounter create(Encounter encounter) {
        LOG.debug("Request to create Encounter : {}", encounter);

        if (encounter.getPatient() == null || encounter.getPatient().getId() == null) {
            throw new BadRequestAlertException("patient is required", "encounter", "patient.required");
        }

        // تأكد أن المريض موجود (اختياري لكنه مفيد لرسالة خطأ أوضح)
        Long pid = encounter.getPatient().getId();
        if (!patientRepository.existsById(pid)) {
            throw new BadRequestAlertException("Patient not found with id " + pid, "patient", "notfound");
        }

        // ممكن برضو تستخدم getReferenceById لتثبيت العلاقة بدون SELECT إضافي
        encounter.setPatient(patientRepository.getReferenceById(pid));

        return encounterRepository.save(encounter);
    }

    public Optional<Encounter> update(Long id, Encounter patch) {
        LOG.debug("Request to update Encounter id={} with {}", id, patch);

        Encounter existing = encounterRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Encounter not found with id " + id, "encounter", "notfound"));

        if (patch.getPatient() != null && patch.getPatient().getId() != null
                && !patch.getPatient().getId().equals(
                existing.getPatient() == null ? null : existing.getPatient().getId())) {

            Long pid = patch.getPatient().getId();
            if (!patientRepository.existsById(pid)) {
                throw new BadRequestAlertException("Patient not found with id " + pid, "patient", "notfound");
            }
            existing.setPatient(patientRepository.getReferenceById(pid));
        }

        if (patch.getAge() != null)     existing.setAge(patch.getAge());
        if (patch.getStatus() != null)  existing.setStatus(patch.getStatus());
        if (patch.getResource() != null)existing.setResource(patch.getResource());
        if (patch.getVisit() != null)   existing.setVisit(patch.getVisit());
        if (patch.getLastModifiedBy() != null) existing.setLastModifiedBy(patch.getLastModifiedBy());

        return Optional.of(encounterRepository.save(existing));
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
    public List<Encounter> findAll() {
        LOG.debug("Request to get all Encounters");
        return encounterRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Encounter> findByStatus(Status status) {
        LOG.debug("Request to get Encounters by status={}", status);
        return encounterRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Encounter> findByPatientId(Long patientId) {
        LOG.debug("Request to get Encounters by patientId={}", patientId);
        return encounterRepository.findByPatient_Id(patientId);
    }

    @Transactional(readOnly = true)
    public List<Encounter> findByResource(Resource resource) {
        LOG.debug("Request to get Encounters by resource={}", resource);
        return encounterRepository.findByResource(resource);
    }

}
