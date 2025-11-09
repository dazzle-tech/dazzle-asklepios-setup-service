package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticTestRadiology;
import com.dazzle.asklepios.service.DiagnosticTestRadiologyService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.radiology.DiagnosticTestRadiologyCreateVM;
import com.dazzle.asklepios.web.rest.vm.radiology.DiagnosticTestRadiologyResponseVM;
import com.dazzle.asklepios.web.rest.vm.radiology.DiagnosticTestRadiologyUpdateVM;
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

/**
 * REST controller for managing {@link com.dazzle.asklepios.domain.DiagnosticTestRadiology}.
 */
@RestController
@RequestMapping("/api/setup/diagnostic-test-radiology")
public class DiagnosticTestRadiologyController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestRadiologyController.class);
    private final DiagnosticTestRadiologyService service;

    public DiagnosticTestRadiologyController(DiagnosticTestRadiologyService service) {
        this.service = service;
    }

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------
    @PostMapping
    public ResponseEntity<DiagnosticTestRadiologyResponseVM> create(
            @Valid @RequestBody DiagnosticTestRadiologyCreateVM vm) {

        LOG.debug("REST create DiagnosticTestRadiology payload={}", vm);
        DiagnosticTestRadiology entity = service.create(DiagnosticTestRadiologyCreateVM.toEntity(vm));
        DiagnosticTestRadiologyResponseVM response = DiagnosticTestRadiologyResponseVM.fromEntity(entity);
        LOG.debug("REST create DiagnosticTestRadiology response={}", response);

        return ResponseEntity
                .created(URI.create("/api/setup/diagnostic-test-radiology/" + entity.getId()))
                .body(response);
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<DiagnosticTestRadiologyResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody DiagnosticTestRadiologyUpdateVM vm) {

        LOG.debug("REST update DiagnosticTestRadiology id={} payload={}", id, vm);
        return service.update(id, DiagnosticTestRadiologyUpdateVM.toEntity(vm))
                .map(DiagnosticTestRadiologyResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------------------------
    // LIST ALL (Paginated)
    // -----------------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<DiagnosticTestRadiologyResponseVM>> findAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST list DiagnosticTestRadiology page={}", pageable);
        Page<DiagnosticTestRadiology> page = service.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(DiagnosticTestRadiologyResponseVM::fromEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // -----------------------------------------------------------------------
    // GET BY ID
    // -----------------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<DiagnosticTestRadiologyResponseVM> findOne(@PathVariable Long id) {
        LOG.debug("REST get DiagnosticTestRadiology id={}", id);
        return service.findOne(id)
                .map(DiagnosticTestRadiologyResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete DiagnosticTestRadiology id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------------
    // GET BY TEST ID (unique)
    // -----------------------------------------------------------------------
    @GetMapping("/by-test/{testId}")
    public ResponseEntity<DiagnosticTestRadiologyResponseVM> findByTestId(@PathVariable Long testId) {
        LOG.debug("REST request to get DiagnosticTestRadiology by testId={}", testId);
        return service.findByTestId(testId)
                .map(DiagnosticTestRadiologyResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    LOG.warn("No DiagnosticTestRadiology found for testId={}", testId);
                    return ResponseEntity.notFound().build();
                });
    }
}
