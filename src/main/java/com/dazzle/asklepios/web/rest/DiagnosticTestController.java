package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.service.DiagnosticTestService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestCreateVM;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestResponseVM;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/setup")
public class DiagnosticTestController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestController.class);
    private final DiagnosticTestService service;

    public DiagnosticTestController(DiagnosticTestService service) {
        this.service = service;
    }

    /**
     * Create new diagnostic test.
     */
    @PostMapping("/diagnostic-test")
    public ResponseEntity<DiagnosticTestResponseVM> create(@RequestBody DiagnosticTestCreateVM vm) {
        LOG.debug("REST request to create DiagnosticTest payload={}", vm);
        DiagnosticTest test = service.create(vm);
        DiagnosticTestResponseVM response = DiagnosticTestResponseVM.ofEntity(test);
        LOG.debug("REST created DiagnosticTest id={} response={}", test.getId(), response);
        return ResponseEntity.created(URI.create("/api/setup/diagnostic-test/" + test.getId()))
                .body(response);
    }

    /**
     * Update existing diagnostic test.
     */
    @PutMapping("/diagnostic-test/{id}")
    public ResponseEntity<DiagnosticTestResponseVM> update(@PathVariable Long id, @RequestBody DiagnosticTestUpdateVM vm) {
        LOG.debug("REST request to update DiagnosticTest id={} payload={}", id, vm);
        return service.update(id, vm)
                .map(updated -> {
                    LOG.debug("REST updated DiagnosticTest id={}", id);
                    return ResponseEntity.ok(DiagnosticTestResponseVM.ofEntity(updated));
                })
                .orElseGet(() -> {
                    LOG.debug("REST DiagnosticTest not found for id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * List all diagnostic tests (paginated).
     */
    @GetMapping("/diagnostic-test")
    public ResponseEntity<List<DiagnosticTestResponseVM>> list(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to list DiagnosticTests page={}", pageable);
        Page<DiagnosticTest> page = service.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        LOG.debug("REST found {} DiagnosticTests", page.getTotalElements());
        return new ResponseEntity<>(page.getContent().stream().map(DiagnosticTestResponseVM::ofEntity).toList(), headers, HttpStatus.OK);
    }
    @GetMapping("/diagnostic-test/active")
    public ResponseEntity<List<DiagnosticTestResponseVM>> getAllActiveTests(Pageable pageable) {
        LOG.debug("REST request to list DiagnosticTests page={}", pageable);
        Page<DiagnosticTest> page = service.findAllActive(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        LOG.debug("REST found {} DiagnosticTests", page.getTotalElements());
        return new ResponseEntity<>(page.getContent().stream().map(DiagnosticTestResponseVM::ofEntity).toList(), headers, HttpStatus.OK);

    }

    /**
     * Find diagnostic tests by type.
     */
    @GetMapping("/diagnostic-test/by-type/{type}")
    public ResponseEntity<List<DiagnosticTestResponseVM>> findByType(@PathVariable TestType type, @ParameterObject Pageable pageable) {
        LOG.debug("REST request to find DiagnosticTests by type={} page={}", type, pageable);
        Page<DiagnosticTest> page = service.findByType(type, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        LOG.debug("REST found {} DiagnosticTests of type={}", page.getTotalElements(), type);
        return new ResponseEntity<>(page.getContent().stream().map(DiagnosticTestResponseVM::ofEntity).toList(), headers, HttpStatus.OK);
    }

    /**
     * Search diagnostic tests by name (case-insensitive).
     */
    @GetMapping("/diagnostic-test/by-name/{name}")
    public ResponseEntity<List<DiagnosticTestResponseVM>> findByName(@PathVariable String name, @ParameterObject Pageable pageable) {
        LOG.debug("REST request to search DiagnosticTests by name='{}' page={}", name, pageable);
        Page<DiagnosticTest> page = service.findByName(name, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        LOG.debug("REST found {} DiagnosticTests matching name='{}'", page.getTotalElements(), name);
        return new ResponseEntity<>(page.getContent().stream().map(DiagnosticTestResponseVM::ofEntity).toList(), headers, HttpStatus.OK);
    }

    /**
     * Get single diagnostic test by id.
     */
    @GetMapping("/diagnostic-test/{id}")
    public ResponseEntity<DiagnosticTestResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST request to get DiagnosticTest id={}", id);
        return service.findOne(id)
                .map(DiagnosticTestResponseVM::ofEntity)
                .map(response -> {
                    LOG.debug("REST found DiagnosticTest id={}", id);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    LOG.debug("REST DiagnosticTest not found id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }
    @PatchMapping("/diagnostic-test/{id}/toggle-active")
    public ResponseEntity<DiagnosticTestResponseVM> togglePractitionerActiveStatus(@PathVariable Long id) {
        LOG.debug("REST toggle Diagnostic Setup isActive id={}", id);
        return service.toggleIsActive(id)
                .map(DiagnosticTestResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}