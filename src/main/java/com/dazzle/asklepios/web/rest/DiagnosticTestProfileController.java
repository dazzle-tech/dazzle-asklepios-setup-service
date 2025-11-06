package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestLaboratory;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.service.DiagnosticTestProfileService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.profile.DiagnosticTestProfileCreateVM;
import com.dazzle.asklepios.web.rest.vm.profile.DiagnosticTestProfileResponseVM;
import com.dazzle.asklepios.web.rest.vm.profile.DiagnosticTestProfileUpdateVM;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/setup/diagnostic-test-profiles")
public class DiagnosticTestProfileController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestProfileController.class);
    private final DiagnosticTestProfileService service;

    public DiagnosticTestProfileController(DiagnosticTestProfileService service) {
        this.service = service;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<DiagnosticTestProfileResponseVM> create(
            @Valid @RequestBody DiagnosticTestProfileCreateVM vm) {

        DiagnosticTestProfile entity = mapToEntity(vm);
        DiagnosticTestProfile saved = service.create(entity);

        LOG.info("Created DiagnosticTestProfile id={} for testId={}",
                saved.getId(), saved.getTest().getId());

        return ResponseEntity.created(URI.create("/api/setup/diagnostic-test-profiles/" + saved.getId()))
                .body(mapToResponse(saved));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<DiagnosticTestProfileResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody DiagnosticTestProfileUpdateVM vm) {

        LOG.debug("REST request to update DiagnosticTestProfile id={}", id);
        return service.update(id, mapToEntity(vm))
                .map(updated -> ResponseEntity.ok(mapToResponse(updated)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET ALL (paginated)
    @GetMapping
    public ResponseEntity<List<DiagnosticTestProfileResponseVM>> findAll(Pageable pageable) {
        LOG.debug("REST request to get all DiagnosticTestProfiles");

        Page<DiagnosticTestProfile> page = service.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        LOG.info("Returned {} DiagnosticTestProfiles records", page.getTotalElements());
        return new ResponseEntity<>(
                page.getContent().stream().map(this::mapToResponse).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // GET ALL BY TEST ID
    @GetMapping("/by-test/{testId}")
    public ResponseEntity<List<DiagnosticTestProfileResponseVM>> findAllByTestId(@PathVariable Long testId) {
        LOG.debug("REST request to get all profiles by testId={}", testId);
        List<DiagnosticTestProfileResponseVM> list = service.findAllByTestId(testId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    // DELETE BY ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete DiagnosticTestProfile id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE ALL BY TEST ID
    @DeleteMapping("/by-test/{testId}")
    public ResponseEntity<Void> deleteAllByTestId(@PathVariable Long testId) {
        LOG.debug("REST request to delete all DiagnosticTestProfiles for testId={}", testId);
        service.deleteAllByTestId(testId);
        return ResponseEntity.noContent().build();
    }

    // ---------- Mapping Helpers ----------

    private DiagnosticTestProfile mapToEntity(DiagnosticTestProfileCreateVM vm) {
        return DiagnosticTestProfile.builder()
                .test(DiagnosticTest.builder().id(vm.testId()).build())
                .name(vm.name())
                .resultUnit(vm.resultUnit())
                .build();
    }

    private DiagnosticTestProfile mapToEntity(DiagnosticTestProfileUpdateVM vm) {
        return DiagnosticTestProfile.builder()
                .id(vm.id())
                .test(DiagnosticTest.builder().id(vm.testId()).build())
                .name(vm.name())
                .resultUnit(vm.resultUnit())
                .build();
    }

    private DiagnosticTestProfileResponseVM mapToResponse(DiagnosticTestProfile e) {
        return new DiagnosticTestProfileResponseVM(
                e.getId(),
                e.getTest() != null ? e.getTest().getId() : null,
                e.getName(),
                e.getResultUnit()
        );
    }
}
