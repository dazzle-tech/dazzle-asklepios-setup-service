package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DischargePlanning;
import com.dazzle.asklepios.service.DischargePlanningService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.discharge.DischargePlanningResponseVM;
import com.dazzle.asklepios.web.rest.vm.discharge.DischargePlanningSaveVM;
import com.dazzle.asklepios.web.rest.vm.discharge.DischargePlanningUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class DischargePlanningController {

    private static final Logger LOG = LoggerFactory.getLogger(DischargePlanningController.class);

    private final DischargePlanningService service;

    public DischargePlanningController(DischargePlanningService service) {
        this.service = service;
    }

    /**
     * Upsert by encounter (Create or Update)
     * FE uses this on Save
     */
    @PostMapping("/discharge-planning")
    public ResponseEntity<DischargePlanningResponseVM> create(@RequestBody DischargePlanningSaveVM vm) {
        LOG.debug("REST upsert DischargePlanning payload={}", vm);

        DischargePlanning saved = service.create(vm);
        URI location = URI.create("/api/medical-sheets/discharge-planning/" + saved.getId());
        return ResponseEntity.ok().location(location).body(DischargePlanningResponseVM.ofEntity(saved));
    }

    // Optional explicit update (if used somewhere)
    @PutMapping("/discharge-planning")
    public ResponseEntity<DischargePlanningResponseVM> update(@RequestBody DischargePlanningUpdateVM vm) {
        DischargePlanning saved = service.update(vm);
        URI location = URI.create("/api/medical-sheets/discharge-planning/" + saved.getId());
        return ResponseEntity.ok().location(location).body(DischargePlanningResponseVM.ofEntity(saved));
    }

    /**
     * GET object by encounter (one-to-one)
     * FE calls this on page load
     */
    @GetMapping("/discharge-planning/by-encounter/{encounterId}")
    public ResponseEntity<DischargePlanningResponseVM> getByEncounter(@PathVariable Long encounterId) {
        LOG.debug("REST get DischargePlanning by encounterId={}", encounterId);

        return service.getObjectByEncounterId(encounterId)
                .map(DischargePlanningResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    // Optional list endpoints
    @GetMapping("/discharge-planning/by-patient/{patientId}")
    public ResponseEntity<List<DischargePlanningResponseVM>> getByPatient(
            @PathVariable Long patientId,
            @ParameterObject Pageable pageable
    ) {
        Page<DischargePlanning> page = service.getByPatient(patientId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(DischargePlanningResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/discharge-planning/by-patient/{patientId}/encounter/{encounterId}")
    public ResponseEntity<List<DischargePlanningResponseVM>> getByPatientAndEncounter(
            @PathVariable Long patientId,
            @PathVariable Long encounterId,
            @ParameterObject Pageable pageable
    ) {
        Page<DischargePlanning> page = service.getByPatientAndEncounter(patientId, encounterId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(DischargePlanningResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }
}
