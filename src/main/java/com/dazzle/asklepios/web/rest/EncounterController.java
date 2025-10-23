package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.Status;
import com.dazzle.asklepios.domain.enumeration.Resource;
import com.dazzle.asklepios.domain.Encounter;
import com.dazzle.asklepios.service.EncounterService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup/encounter")
public class EncounterController {

    private static final Logger LOG = LoggerFactory.getLogger(EncounterController.class);

    private final EncounterService encounterService;

    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Encounter> createEncounter(@Valid @RequestBody Encounter encounter) {
        LOG.debug("REST create Encounter payload={}", encounter);
        Encounter result = encounterService.create(encounter);
        return ResponseEntity
                .created(URI.create("/api/setup/encounter/" + result.getId()))
                .body(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Encounter> updateEncounter(@PathVariable Long id, @RequestBody Encounter patch){
        LOG.debug("REST update Encounter id={} payload={}", id, patch);
        return encounterService.update(id, patch)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** GET /api/setup/encounter — بدون فلاتر، يرجّع كل Encounter */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Encounter>> getAllEncounters() {
        LOG.debug("REST list Encounters (no filters)");
        List<Encounter> all = encounterService.findAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping(value = "/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Encounter>> getByStatus(@PathVariable Status status) {
        LOG.debug("REST list Encounters by status={}", status);
        return ResponseEntity.ok(encounterService.findByStatus(status));
    }

    @GetMapping(value = "/resource/{resource}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Encounter>> findByResource(@PathVariable Resource resource){
        LOG.debug("REST list Encounters by resource={}", resource);
        return  ResponseEntity.ok(encounterService.findByResource(resource));
    }

    @GetMapping(value = "/patient/{patientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Encounter>> findByPatientId(@PathVariable Long patientId){
        LOG.debug("REST list Encounters by patientId={}", patientId);
        return  ResponseEntity.ok(encounterService.findByPatientId(patientId));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Encounter> getEncounter(@PathVariable Long id) {
        LOG.debug("REST get Encounter id={}", id);
        return encounterService.findOne(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteEncounter(@PathVariable Long id) {
        LOG.debug("REST delete Encounter id={}", id);
        encounterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
