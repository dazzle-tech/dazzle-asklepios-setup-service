package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestLaboratory;
import com.dazzle.asklepios.service.DiagnosticTestLaboratoryService;
import com.dazzle.asklepios.web.rest.vm.laboratory.DiagnosticTestLaboratoryCreateVM;
import com.dazzle.asklepios.web.rest.vm.laboratory.DiagnosticTestLaboratoryResponseVM;
import com.dazzle.asklepios.web.rest.vm.laboratory.DiagnosticTestLaboratoryUpdateVM;
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

/**
 * REST controller for managing {@link com.dazzle.asklepios.domain.DiagnosticTestLaboratory}.
 */
@RestController
@RequestMapping("/api/setup/diagnostic-test-laboratories")
public class DiagnosticTestLaboratoryController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestLaboratoryController.class);

    private final DiagnosticTestLaboratoryService service;

    public DiagnosticTestLaboratoryController(DiagnosticTestLaboratoryService service) {
        this.service = service;
    }

    /**
     * {@code POST  /diagnostic-test-laboratories} : Create a new DiagnosticTestLaboratory.
     *
     * @param vm the data to create DiagnosticTestLaboratory.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and the created object.
     */
    @PostMapping
    public ResponseEntity<DiagnosticTestLaboratoryResponseVM> create(@Valid @RequestBody DiagnosticTestLaboratoryCreateVM vm) {
        LOG.debug("REST request to create DiagnosticTestLaboratory : {}", vm);

        DiagnosticTestLaboratory entity = mapToEntity(vm);
        DiagnosticTestLaboratory saved = service.create(entity);

        LOG.info("Created DiagnosticTestLaboratory with id={}", saved.getId());
        return ResponseEntity.created(URI.create("/api/diagnostic-test-laboratories/" + saved.getId()))
                .body(mapToResponse(saved));
    }

    /**
     * {@code PUT  /diagnostic-test-laboratories/:id} : Updates an existing DiagnosticTestLaboratory.
     *
     * @param id the id of the object to update.
     * @param vm the update data.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the updated object,
     *         or {@code 404 (Not Found)} if not found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiagnosticTestLaboratoryResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody DiagnosticTestLaboratoryUpdateVM vm) {

        LOG.debug("REST request to update DiagnosticTestLaboratory id={} with {}", id, vm);

        Optional<DiagnosticTestLaboratory> updated = service.update(id, mapToEntity(vm));
        if (updated.isPresent()) {
            LOG.info("Updated DiagnosticTestLaboratory id={}", id);
            return ResponseEntity.ok(mapToResponse(updated.get()));
        } else {
            LOG.warn("DiagnosticTestLaboratory id={} not found for update", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * {@code GET  /diagnostic-test-laboratories} : Get all DiagnosticTestLaboratories.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of results.
     */
    @GetMapping
    public ResponseEntity<Page<DiagnosticTestLaboratoryResponseVM>> findAll(Pageable pageable) {
        LOG.debug("REST request to get all DiagnosticTestLaboratories");

        Page<DiagnosticTestLaboratoryResponseVM> page = service.findAll(pageable).map(this::mapToResponse);
        LOG.info("Returned {} DiagnosticTestLaboratory records", page.getTotalElements());
        return ResponseEntity.ok(page);
    }

    /**
     * {@code GET  /diagnostic-test-laboratories/:id} : Get a specific DiagnosticTestLaboratory by id.
     *
     * @param id the id of the object.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the object, or {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiagnosticTestLaboratoryResponseVM> findOne(@PathVariable Long id) {
        LOG.debug("REST request to get DiagnosticTestLaboratory id={}", id);

        return service.findOne(id)
                .map(e -> {
                    LOG.info("Found DiagnosticTestLaboratory id={}", id);
                    return ResponseEntity.ok(mapToResponse(e));
                })
                .orElseGet(() -> {
                    LOG.warn("DiagnosticTestLaboratory id={} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * {@code DELETE  /diagnostic-test-laboratories/:id} : Delete a DiagnosticTestLaboratory by id.
     *
     * @param id the id of the object to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete DiagnosticTestLaboratory id={}", id);

        service.delete(id);
        LOG.info("Deleted DiagnosticTestLaboratory id={}", id);
        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------------
    // Mapping Helpers
    // -----------------------------------------------------------------------

    private DiagnosticTestLaboratory mapToEntity(DiagnosticTestLaboratoryCreateVM vm) {
        return DiagnosticTestLaboratory.builder()
                .test(DiagnosticTest.builder().id(vm.testId()).build())
                .property(vm.property())
                .system(vm.system())
                .scale(vm.scale())
                .reagents(vm.reagents())
                .method(vm.method())
                .testDurationTime(vm.testDurationTime())
                .timeUnit(vm.timeUnit())
                .resultUnit(vm.resultUnit())
                .isProfile(vm.isProfile() != null ? vm.isProfile() : false)
                .sampleContainer(vm.sampleContainer())
                .sampleVolume(vm.sampleVolume())
                .sampleVolumeUnit(vm.sampleVolumeUnit())
                .tubeColor(vm.tubeColor())
                .testDescription(vm.testDescription())
                .sampleHandling(vm.sampleHandling())
                .turnaroundTime(vm.turnaroundTime())
                .turnaroundTimeUnit(vm.turnaroundTimeUnit())
                .preparationRequirements(vm.preparationRequirements())
                .medicalIndications(vm.medicalIndications())
                .associatedRisks(vm.associatedRisks())
                .testInstructions(vm.testInstructions())
                .category(vm.category())
                .tubeType(vm.tubeType())
                .build();
    }

    private DiagnosticTestLaboratory mapToEntity(DiagnosticTestLaboratoryUpdateVM vm) {
        DiagnosticTestLaboratory entity = mapToEntity(new DiagnosticTestLaboratoryCreateVM(
                vm.testId(), vm.property(), vm.system(), vm.scale(), vm.reagents(), vm.method(),
                vm.testDurationTime(), vm.timeUnit(), vm.resultUnit(), vm.isProfile(),
                vm.sampleContainer(), vm.sampleVolume(), vm.sampleVolumeUnit(), vm.tubeColor(),
                vm.testDescription(), vm.sampleHandling(), vm.turnaroundTime(), vm.turnaroundTimeUnit(),
                vm.preparationRequirements(), vm.medicalIndications(), vm.associatedRisks(),
                vm.testInstructions(), vm.category(), vm.tubeType()
        ));
        entity.setId(vm.id());
        return entity;
    }

    private DiagnosticTestLaboratoryResponseVM mapToResponse(DiagnosticTestLaboratory e) {
        return new DiagnosticTestLaboratoryResponseVM(
                e.getId(),
                e.getTest() != null ? e.getTest().getId() : null,
                e.getProperty(),
                e.getSystem(),
                e.getScale(),
                e.getReagents(),
                e.getMethod(),
                e.getTestDurationTime(),
                e.getTimeUnit(),
                e.getResultUnit(),
                e.getIsProfile(),
                e.getSampleContainer(),
                e.getSampleVolume(),
                e.getSampleVolumeUnit(),
                e.getTubeColor(),
                e.getTestDescription(),
                e.getSampleHandling(),
                e.getTurnaroundTime(),
                e.getTurnaroundTimeUnit(),
                e.getPreparationRequirements(),
                e.getMedicalIndications(),
                e.getAssociatedRisks(),
                e.getTestInstructions(),
                e.getCategory(),
                e.getTubeType()
        );
    }
}
