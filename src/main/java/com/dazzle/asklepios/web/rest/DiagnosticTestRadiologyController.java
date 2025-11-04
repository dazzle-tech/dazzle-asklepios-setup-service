package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestRadiology;
import com.dazzle.asklepios.service.DiagnosticTestRadiologyService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.radiology.DiagnosticTestRadiologyCreateVM;
import com.dazzle.asklepios.web.rest.vm.radiology.DiagnosticTestRadiologyResponseVM;
import com.dazzle.asklepios.web.rest.vm.radiology.DiagnosticTestRadiologyUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/setup/diagnostic-test-radiology")
public class DiagnosticTestRadiologyController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestRadiologyController.class);
    private final DiagnosticTestRadiologyService service;

    public DiagnosticTestRadiologyController(DiagnosticTestRadiologyService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DiagnosticTestRadiologyResponseVM> create(@RequestBody DiagnosticTestRadiologyCreateVM vm) {
        LOG.debug("REST request to create DiagnosticTestRadiology: {}", vm);
        DiagnosticTestRadiology entity = mapToEntity(vm);
        DiagnosticTestRadiology saved = service.create(entity);
        return ResponseEntity.created(URI.create("/api/setup/diagnostic-test-radiology/" + saved.getId()))
                .body(mapToResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiagnosticTestRadiologyResponseVM> update(@PathVariable Long id, @RequestBody DiagnosticTestRadiologyUpdateVM vm) {
        LOG.debug("REST request to update DiagnosticTestRadiology id={} with {}", id, vm);
        Optional<DiagnosticTestRadiology> updated = service.update(id, mapToEntity(vm));
        return updated.map(e -> ResponseEntity.ok(mapToResponse(e)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping
    public ResponseEntity<List<DiagnosticTestRadiologyResponseVM>> findAll(Pageable pageable) {
        LOG.debug("REST request to get all DiagnosticTestRadiologies");

        Page<DiagnosticTestRadiology> page = service.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        LOG.info("Returned {} DiagnosticTestRadiology records", page.getTotalElements());
        return new ResponseEntity<>(
                page.getContent().stream().map(this::mapToResponse).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiagnosticTestRadiologyResponseVM> findOne(@PathVariable Long id) {
        return service.findOne(id)
                .map(e -> ResponseEntity.ok(mapToResponse(e)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-test/{testId}")
    public ResponseEntity<DiagnosticTestRadiologyResponseVM> findByTestId(@PathVariable Long testId) {
        return service.findByTestId(testId)
                .map(e -> ResponseEntity.ok(mapToResponse(e)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Mapping helpers
    private DiagnosticTestRadiology mapToEntity(DiagnosticTestRadiologyCreateVM vm) {
        return DiagnosticTestRadiology.builder()
                .test(DiagnosticTest.builder().id(vm.testId()).build())
                .category(vm.category())
                .imageDuration(vm.imageDuration())
                
                .testInstructions(vm.testInstructions())
                .medicalIndications(vm.medicalIndications())
                .turnaroundTimeUnit(vm.turnaroundTimeUnit())
                .turnaroundTime(vm.turnaroundTime())
                .associatedRisks(vm.associatedRisks())

                .build();
    }

    private DiagnosticTestRadiology mapToEntity(DiagnosticTestRadiologyUpdateVM vm) {
        DiagnosticTestRadiology entity = new DiagnosticTestRadiology();
        entity.setId(vm.id());
        entity.setTest(DiagnosticTest.builder().id(vm.testId()).build());
        entity.setCategory(vm.category());
        entity.setImageDuration(vm.imageDuration());

        entity.setTestInstructions(vm.testInstructions());
        entity.setMedicalIndications(vm.medicalIndications());
        entity.setTurnaroundTimeUnit(vm.turnaroundTimeUnit());
        entity.setTurnaroundTime(vm.turnaroundTime());
        entity.setAssociatedRisks(vm.associatedRisks());
        return entity;
    }

    private DiagnosticTestRadiologyResponseVM mapToResponse(DiagnosticTestRadiology e) {
        return new DiagnosticTestRadiologyResponseVM(
                e.getId(),
                e.getTest() != null ? e.getTest().getId() : null,
                e.getCategory(),
                e.getImageDuration(),

                e.getTestInstructions(),
                e.getMedicalIndications(),
                e.getTurnaroundTimeUnit(),
                e.getTurnaroundTime(),
                e.getAssociatedRisks()
        );
    }
}
