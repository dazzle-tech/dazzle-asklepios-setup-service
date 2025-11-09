package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.service.DiagnosticTestProfileService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.profile.DiagnosticTestProfileCreateVM;
import com.dazzle.asklepios.web.rest.vm.profile.DiagnosticTestProfileResponseVM;
import com.dazzle.asklepios.web.rest.vm.profile.DiagnosticTestProfileUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
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

        LOG.debug("REST create DiagnosticTestProfile payload={}", vm);
        DiagnosticTestProfile saved = service.create(vm.toEntity());
        return ResponseEntity
                .created(URI.create("/api/setup/diagnostic-test-profiles/" + saved.getId()))
                .body(DiagnosticTestProfileResponseVM.fromEntity(saved));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<DiagnosticTestProfileResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody DiagnosticTestProfileUpdateVM vm) {

        LOG.debug("REST update DiagnosticTestProfile id={} payload={}", id, vm);
        return service.update(id, vm.toEntity())
                .map(DiagnosticTestProfileResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET ALL (Paginated)
    @GetMapping
    public ResponseEntity<List<DiagnosticTestProfileResponseVM>> findAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST list DiagnosticTestProfiles page={}", pageable);
        Page<DiagnosticTestProfile> page = service.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(DiagnosticTestProfileResponseVM::fromEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // GET ALL BY TEST ID (Paginated)
    @GetMapping("/by-test/{testId}")
    public ResponseEntity<List<DiagnosticTestProfileResponseVM>> findAllByTestId(
            @PathVariable Long testId,
            @ParameterObject Pageable pageable) {

        LOG.debug("REST list DiagnosticTestProfiles by testId={} page={}", testId, pageable);
        Page<DiagnosticTestProfile> page = service.findAllByTestId(testId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(DiagnosticTestProfileResponseVM::fromEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // DELETE ONE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete DiagnosticTestProfile id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE ALL BY TEST ID
    @DeleteMapping("/by-test/{testId}")
    public ResponseEntity<Void> deleteAllByTestId(@PathVariable Long testId) {
        LOG.debug("REST delete all DiagnosticTestProfiles for testId={}", testId);
        service.deleteAllByTestId(testId);
        return ResponseEntity.noContent().build();
    }
}
