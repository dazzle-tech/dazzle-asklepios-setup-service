package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.Status;
import com.dazzle.asklepios.domain.enumeration.Resource;
import com.dazzle.asklepios.domain.Encounter;
import com.dazzle.asklepios.service.EncounterService;
import com.dazzle.asklepios.web.rest.vm.EncounterCreateVM;
import com.dazzle.asklepios.web.rest.vm.EncounterResponseVM;
import com.dazzle.asklepios.web.rest.vm.EncounterUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class EncounterController {

    private static final Logger LOG = LoggerFactory.getLogger(EncounterController.class);

    private final EncounterService encounterService;

    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @PostMapping("/encounter")
    public ResponseEntity<EncounterResponseVM> create(@RequestParam Long patientId,@Valid @RequestBody EncounterCreateVM encounterVM) {
        LOG.debug("REST create Encounter patientId={}, payload={}", patientId, encounterVM);

        Encounter toCreate = Encounter.builder()
                .resourceType(encounterVM.resourceType())
                .visitType(encounterVM.visitType())
                .age(encounterVM.age())
                .status(encounterVM.status())
                .build();
        Encounter created = encounterService.create(toCreate,patientId);
        EncounterResponseVM body = EncounterResponseVM.ofEntity(created);
        return ResponseEntity
                .created(URI.create("/api/setup/encounter/" + created.getId() + "?patientId=" + patientId))
                .body(body);
    }

    @PutMapping("/encounter/{id}")
    public ResponseEntity<EncounterResponseVM> updateEncounter(@PathVariable Long id, @RequestParam Long patientId ,@Valid @RequestBody EncounterUpdateVM encounterVM){
        LOG.debug("REST update Encounter id={} patientId={} payload={}", id, patientId, encounterVM);

        Encounter toUpdate = Encounter.builder()
                .resourceType(encounterVM.resourceType())
                .visitType(encounterVM.visitType())
                .age(encounterVM.age())
                .status(encounterVM.status())
                .build();

        return encounterService.update(id, patientId, toUpdate)
                .map(EncounterResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** GET /api/setup/encounter — بدون فلاتر، يرجّع كل Encounter */
    @GetMapping(value = "/encounter", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Encounter>> getAllEncounters() {
        LOG.debug("REST list Encounters (no filters)");
        List<Encounter> all = encounterService.findAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping(value = "/encounter/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Encounter>> getByStatus(@PathVariable Status status) {
        LOG.debug("REST list Encounters by status={}", status);
        return ResponseEntity.ok(encounterService.findByStatus(status));
    }

    @GetMapping(value = "/encounter/resource/{resource}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Encounter>> findByResource(@PathVariable Resource resource){
        LOG.debug("REST list Encounters by resource={}", resource);
        return  ResponseEntity.ok(encounterService.findByResource(resource));
    }

    @GetMapping(value = "/encounter/patient/{patientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Encounter>> findByPatientId(@PathVariable Long patientId){
        LOG.debug("REST list Encounters by patientId={}", patientId);
        return  ResponseEntity.ok(encounterService.findByPatientId(patientId));
    }

    @GetMapping(value = "/encounter/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Encounter> getEncounter(@PathVariable Long id) {
        LOG.debug("REST get Encounter id={}", id);
        return encounterService.findOne(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = "/encounter/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteEncounter(@PathVariable Long id) {
        LOG.debug("REST delete Encounter id={}", id);
        encounterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
