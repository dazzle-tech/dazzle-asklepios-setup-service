package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticTestPathology;
import com.dazzle.asklepios.service.DiagnosticTestPathologyService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.pathology.DiagnosticTestPathologyCreateVM;
import com.dazzle.asklepios.web.rest.vm.pathology.DiagnosticTestPathologyResponseVM;
import com.dazzle.asklepios.web.rest.vm.pathology.DiagnosticTestPathologyUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing {@link com.dazzle.asklepios.domain.DiagnosticTestPathology}.
 */
@RestController
@RequestMapping("/api/setup/diagnostic-test-pathology")
public class DiagnosticTestPathologyController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestPathologyController.class);
    private final DiagnosticTestPathologyService service;

    public DiagnosticTestPathologyController(DiagnosticTestPathologyService service) {
        this.service = service;
    }

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------
    @PostMapping
    public ResponseEntity<DiagnosticTestPathologyResponseVM> create(
            @Valid @RequestBody DiagnosticTestPathologyCreateVM vm) {

        LOG.debug("REST create DiagnosticTestPathology payload={}", vm);
        DiagnosticTestPathology entity = service.create(DiagnosticTestPathologyCreateVM.toEntity(vm));
        DiagnosticTestPathologyResponseVM response = DiagnosticTestPathologyResponseVM.fromEntity(entity);

        return ResponseEntity
                .created(URI.create("/api/setup/diagnostic-test-pathology/" + entity.getId()))
                .body(response);
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<DiagnosticTestPathologyResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody DiagnosticTestPathologyUpdateVM vm) {

        LOG.debug("REST update DiagnosticTestPathology id={} payload={}", id, vm);
        return service.update(id, DiagnosticTestPathologyUpdateVM.toEntity(vm))
                .map(DiagnosticTestPathologyResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------------------------
    // LIST ALL (Paginated)
    // -----------------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<DiagnosticTestPathologyResponseVM>> findAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST list DiagnosticTestPathology page={}", pageable);
        Page<DiagnosticTestPathology> page = service.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(DiagnosticTestPathologyResponseVM::fromEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // -----------------------------------------------------------------------
    // GET BY ID
    // -----------------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<DiagnosticTestPathologyResponseVM> findOne(@PathVariable Long id) {
        LOG.debug("REST get DiagnosticTestPathology id={}", id);
        return service.findOne(id)
                .map(DiagnosticTestPathologyResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------------------------
    // GET BY TEST ID (unique)
    // -----------------------------------------------------------------------
    @GetMapping("/by-test/{testId}")
    public ResponseEntity<DiagnosticTestPathologyResponseVM> findByTestId(@PathVariable Long testId) {
        LOG.debug("REST get DiagnosticTestPathology by testId={}", testId);
        return service.findByTestId(testId)
                .map(DiagnosticTestPathologyResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    LOG.warn("No DiagnosticTestPathology found for testId={}", testId);
                    return ResponseEntity.notFound().build();
                });
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete DiagnosticTestPathology id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
