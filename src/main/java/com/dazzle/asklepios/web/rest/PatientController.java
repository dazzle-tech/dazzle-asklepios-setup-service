package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.service.PatientService;
import com.dazzle.asklepios.web.rest.vm.PatientCreateVM;
import com.dazzle.asklepios.web.rest.vm.PatientResponseVM;
import com.dazzle.asklepios.web.rest.vm.PatientUpdateVM;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/setup")
public class PatientController {

    private static final Logger LOG = LoggerFactory.getLogger(PatientController.class);

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // ================= Utilities =================
    // لازم يتغير
    private static String norm(String s) { return s == null ? null : s.trim(); }
    private static boolean containsIgnoreCase(String value, String filter) {
        if (filter == null || filter.isEmpty()) return true;   // لا فلترة
        if (value == null) return false;
        return value.toLowerCase().contains(filter.toLowerCase());
    }

    // =============================== Endpoints ===============================

    /**
     * POST /api/setup/patient
     */
    @PostMapping("/patient")
    public ResponseEntity<PatientResponseVM> create(@Valid @RequestBody PatientCreateVM patientVm) {
        LOG.debug("REST create Patient payload={}", patientVm);

        Patient toCreate = Patient.builder()
                .firstName(patientVm.firstName())
                .lastName(patientVm.lastName())
                .email(patientVm.email())
                .gender(patientVm.gender())
                .dateOfBirth(patientVm.dateOfBirth())
                .build();
        Patient created = patientService.create(toCreate);
        PatientResponseVM body = PatientResponseVM.ofEntity(created);


        return ResponseEntity
                .created(URI.create("/api/setup/patient/" + created.getId()))
                .body(body);
    }

    /**
     * PUT /api/setup/patient/{id}
     */
    @PutMapping("/patient/{id}")
    public ResponseEntity<PatientResponseVM> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientUpdateVM patchVM) {
        LOG.debug("REST update Patient id={} payload={}", id, patchVM);

        Patient toUpdate = Patient.builder()
                .id(patchVM.id())
                .firstName(patchVM.firstName())
                .lastName(patchVM.lastName())
                .email(patchVM.email())
                .gender(patchVM.gender())
                .dateOfBirth(patchVM.dateOfBirth())
                .build();
        LOG.debug("REST update Patient toUpdate id={} payload={}", id, toUpdate);

        return patientService.update(id, toUpdate)
                .map(PatientResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/setup/patient
     */
    @GetMapping(value = "/patient", produces = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity<List<Patient>> getAllPatients(
    ) {
        LOG.debug("REST list Patients filters:");

        List<Patient> all = patientService.findAll();

        return ResponseEntity.ok(all);
    }

    /**
     * GET /api/setup/patient/{id}
     */
    @GetMapping(value = "/patient/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Patient> getPatient(@PathVariable Long id) {
        LOG.debug("REST get Patient id={}", id);
        return patientService.findOne(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/setup/patient/by-email?email=...
     */
    @GetMapping(value = "/patient/by-email", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Patient> getPatientByEmail(@RequestParam("email") @NotBlank @Email String email) {
        LOG.debug("REST get Patient by email={}", email);
        return patientService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/setup/patient/{id}
     */
    @DeleteMapping(value = "/patient/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        LOG.debug("REST delete Patient id={}", id);
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
