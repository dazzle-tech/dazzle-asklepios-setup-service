package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticTestLaboratory;
import com.dazzle.asklepios.service.DiagnosticTestLaboratoryService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.laboratory.DiagnosticTestLaboratoryCreateVM;
import com.dazzle.asklepios.web.rest.vm.laboratory.DiagnosticTestLaboratoryResponseVM;
import com.dazzle.asklepios.web.rest.vm.laboratory.DiagnosticTestLaboratoryUpdateVM;
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

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------
    @PostMapping
    public ResponseEntity<DiagnosticTestLaboratoryResponseVM> create(
            @Valid @RequestBody DiagnosticTestLaboratoryCreateVM vm) {

        LOG.debug("REST create DiagnosticTestLaboratory payload={}", vm);
        DiagnosticTestLaboratory entity = service.create(vm.toEntity());
        DiagnosticTestLaboratoryResponseVM response = DiagnosticTestLaboratoryResponseVM.fromEntity(entity);
        LOG.debug("REST create DiagnosticTestLaboratory response={}", response);

        return ResponseEntity
                .created(URI.create("/api/setup/diagnostic-test-laboratories/" + entity.getId()))
                .body(response);
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<DiagnosticTestLaboratoryResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody DiagnosticTestLaboratoryUpdateVM vm) {

        LOG.debug("REST update DiagnosticTestLaboratory id={} payload={}", id, vm);
        return service.update(id, vm.toEntity())
                .map(DiagnosticTestLaboratoryResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------------------------
    // LIST ALL (Paginated)
    // -----------------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<DiagnosticTestLaboratoryResponseVM>> findAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST list DiagnosticTestLaboratories page={}", pageable);
        Page<DiagnosticTestLaboratory> page = service.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(DiagnosticTestLaboratoryResponseVM::fromEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // -----------------------------------------------------------------------
    // GET BY ID
    // -----------------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<DiagnosticTestLaboratoryResponseVM> findOne(@PathVariable Long id) {
        LOG.debug("REST get DiagnosticTestLaboratory id={}", id);
        return service.findOne(id)
                .map(DiagnosticTestLaboratoryResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete DiagnosticTestLaboratory id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------------
    // GET BY TEST ID (Paginated)
    // -----------------------------------------------------------------------
    @GetMapping("/by-test/{testId}")
    public ResponseEntity<DiagnosticTestLaboratoryResponseVM> findByTestId(@PathVariable Long testId) {
        LOG.debug("REST request to get DiagnosticTestLaboratory by testId={}", testId);
        try {
            DiagnosticTestLaboratory lab = service.getByTestId(testId);
            LOG.info("Found DiagnosticTestLaboratory for testId={}", testId);
            return ResponseEntity.ok(DiagnosticTestLaboratoryResponseVM.fromEntity(lab));
        } catch (Exception ex) {
            LOG.warn("No DiagnosticTestLaboratory found for testId={}", testId);
            return ResponseEntity.notFound().build();
        }
    }
}
