package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestPathology;
import com.dazzle.asklepios.service.DiagnosticTestPathologyService;
import com.dazzle.asklepios.web.rest.vm.pathology.DiagnosticTestPathologyCreateVM;
import com.dazzle.asklepios.web.rest.vm.pathology.DiagnosticTestPathologyResponseVM;
import com.dazzle.asklepios.web.rest.vm.pathology.DiagnosticTestPathologyUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/setup/diagnostic-test-pathology")
public class DiagnosticTestPathologyController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestPathologyController.class);
    private final DiagnosticTestPathologyService service;

    public DiagnosticTestPathologyController(DiagnosticTestPathologyService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DiagnosticTestPathologyResponseVM> create(@Valid @RequestBody DiagnosticTestPathologyCreateVM vm) {
        LOG.debug("REST request to create DiagnosticTestPathology : {}", vm);
        DiagnosticTestPathology entity = mapToEntity(vm);
        DiagnosticTestPathology saved = service.create(entity);
        return ResponseEntity.created(URI.create("/api/setup/diagnostic-test-pathology/" + saved.getId()))
                .body(mapToResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiagnosticTestPathologyResponseVM> update(@PathVariable Long id, @Valid @RequestBody DiagnosticTestPathologyUpdateVM vm) {
        LOG.debug("REST request to update DiagnosticTestPathology id={}", id);
        Optional<DiagnosticTestPathology> updated = service.update(id, mapToEntity(vm));
        return updated.map(u -> ResponseEntity.ok(mapToResponse(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<DiagnosticTestPathologyResponseVM>> findAll(Pageable pageable) {
        LOG.debug("REST request to get all DiagnosticTestPathology");
        Page<DiagnosticTestPathologyResponseVM> page = service.findAll(pageable).map(this::mapToResponse);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiagnosticTestPathologyResponseVM> findOne(@PathVariable Long id) {
        return service.findOne(id)
                .map(e -> ResponseEntity.ok(mapToResponse(e)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-test/{testId}")
    public ResponseEntity<DiagnosticTestPathologyResponseVM> findByTestId(@PathVariable Long testId) {
        LOG.debug("REST request to get DiagnosticTestPathology by testId={}", testId);
        return service.findByTestId(testId)
                .map(e -> ResponseEntity.ok(mapToResponse(e)))
                .orElseGet(() -> ResponseEntity.ok(new DiagnosticTestPathologyResponseVM(null, testId, null, null, null, null, null, null, null, null, null, null, null)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private DiagnosticTestPathology mapToEntity(DiagnosticTestPathologyCreateVM vm) {
        return DiagnosticTestPathology.builder()
                .test(DiagnosticTest.builder().id(vm.testId()).build())
                .category(vm.category())
                .specimenType(vm.specimenType())
                .analysisProcedure(vm.analysisProcedure())
                .turnaroundTime(vm.turnaroundTime())
                .timeUnit(vm.timeUnit())
                .testDescription(vm.testDescription())
                .sampleHandling(vm.sampleHandling())
                .medicalIndications(vm.medicalIndications())
                .criticalValues(vm.criticalValues())
                .preparationRequirements(vm.preparationRequirements())
                .associatedRisks(vm.associatedRisks())
                .build();
    }

    private DiagnosticTestPathology mapToEntity(DiagnosticTestPathologyUpdateVM vm) {
        DiagnosticTestPathology entity = mapToEntity(new DiagnosticTestPathologyCreateVM(
                vm.testId(), vm.category(), vm.specimenType(), vm.analysisProcedure(), vm.turnaroundTime(), vm.timeUnit(),
                vm.testDescription(), vm.sampleHandling(), vm.medicalIndications(), vm.criticalValues(), vm.preparationRequirements(), vm.associatedRisks()
        ));
        entity.setId(vm.id());
        return entity;
    }

    private DiagnosticTestPathologyResponseVM mapToResponse(DiagnosticTestPathology e) {
        return new DiagnosticTestPathologyResponseVM(
                e.getId(),
                e.getTest() != null ? e.getTest().getId() : null,
                e.getCategory(),
                e.getSpecimenType(),
                e.getAnalysisProcedure(),
                e.getTurnaroundTime(),
                e.getTimeUnit(),
                e.getTestDescription(),
                e.getSampleHandling(),
                e.getMedicalIndications(),
                e.getCriticalValues(),
                e.getPreparationRequirements(),
                e.getAssociatedRisks()
        );
    }
}
