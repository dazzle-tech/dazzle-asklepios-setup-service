package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.Status;
import com.dazzle.asklepios.domain.enumeration.Resource;
import com.dazzle.asklepios.domain.Encounter;
import com.dazzle.asklepios.service.EncounterService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.EncounterCreateVM;
import com.dazzle.asklepios.web.rest.vm.EncounterResponseVM;
import com.dazzle.asklepios.web.rest.vm.EncounterUpdateVM;
import com.dazzle.asklepios.web.rest.vm.ServiceResponseVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    public ResponseEntity<EncounterResponseVM> create(
            @RequestParam Long patientId,
            @Valid @RequestBody EncounterCreateVM encounterVM
    ) {
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
    public ResponseEntity<EncounterResponseVM> updateEncounter(
            @PathVariable Long id,
            @RequestParam Long patientId ,
            @Valid @RequestBody EncounterUpdateVM encounterVM
    ){
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

    @GetMapping(value = "/encounters", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EncounterResponseVM>> getAllEncounters(@ParameterObject Pageable pageable) {
        final Page<Encounter> page = encounterService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        List<EncounterResponseVM> body = page.getContent()
                .stream()
                .map(EncounterResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }


    @GetMapping(value = "/encounter/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EncounterResponseVM>> getByStatus(
            @PathVariable Status status,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Encounters by status={} pageable={}", status, pageable);
        final Page<Encounter> page = encounterService.findByStatus( status, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(EncounterResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping(value = "/encounter/resource/{resource}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EncounterResponseVM>> findByResource(
            @PathVariable Resource resource,
            @ParameterObject Pageable pageable
    ){
        LOG.debug("REST list Encounters by resource={} pageable={}", resource, pageable);
        final Page<Encounter> page = encounterService.findByResource( resource, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(EncounterResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping(value = "/encounter/patient/{patientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EncounterResponseVM>> findByPatientId(
            @PathVariable Long patientId,
            @ParameterObject Pageable pageable
    ){
        LOG.debug("REST list Encounters by patientId={} pageable={}", patientId, pageable);
        final Page<Encounter> page = encounterService.findByPatientId(patientId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(EncounterResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
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
