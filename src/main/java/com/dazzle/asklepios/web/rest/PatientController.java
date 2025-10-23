package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.service.PatientService;
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
@RequestMapping("/api/setup/patient")
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
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Patient> createPatient(@Valid @RequestBody Patient patient) {
        LOG.debug("REST create Patient payload={}", patient);
        Patient result = patientService.create(patient);
        return ResponseEntity
                .created(URI.create("/api/setup/patient/" + result.getId()))
                .body(result);
    }

    /**
     * PUT /api/setup/patient/{id}
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @Valid @RequestBody Patient patch) {
        LOG.debug("REST update Patient id={} payload={}", id, patch);
        return patientService.update(id, patch)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/setup/patient
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Patient>> getAllPatients(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email
    ) {
        LOG.debug("REST list Patients filters: firstName='{}' lastName='{}' email='{}'",
                firstName, lastName, email);

        String fFirst = norm(firstName);
        String fLast  = norm(lastName);
        String fEmail = norm(email);

        List<Patient> all = patientService.findAll();

        List<Patient> filtered = all.stream()
                .filter(p -> containsIgnoreCase(p.getFirstName(), fFirst))
                .filter(p -> containsIgnoreCase(p.getLastName(),  fLast))
                .filter(p -> containsIgnoreCase(p.getEmail(),     fEmail))
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    /**
     * GET /api/setup/patient/{id}
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Patient> getPatient(@PathVariable Long id) {
        LOG.debug("REST get Patient id={}", id);
        return patientService.findOne(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/setup/patient/by-email?email=...
     */
    @GetMapping(value = "/by-email", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Patient> getPatientByEmail(@RequestParam("email") @NotBlank @Email String email) {
        LOG.debug("REST get Patient by email={}", email);
        return patientService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/setup/patient/{id}
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        LOG.debug("REST delete Patient id={}", id);
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
