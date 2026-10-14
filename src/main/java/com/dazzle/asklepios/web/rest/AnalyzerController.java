package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Analyzer;
import com.dazzle.asklepios.service.AnalyzerService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.analyzer.AnalyzerCreateVM;
import com.dazzle.asklepios.web.rest.vm.analyzer.AnalyzerResponseVM;
import com.dazzle.asklepios.web.rest.vm.analyzer.AnalyzerUpdateVM;
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
public class AnalyzerController {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyzerController.class);

    private final AnalyzerService analyzerService;

    public AnalyzerController(AnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    // ====================== CREATE ======================
    @PostMapping("/analyzer")
    public ResponseEntity<AnalyzerResponseVM> createAnalyzer(
            @Valid @RequestBody AnalyzerCreateVM vm
    ) {
        LOG.debug("REST create Analyzer payload={}", vm);

        Analyzer toCreate = Analyzer.builder()
                .code(vm.code())
                .type(vm.type())
                .isActive(vm.isActive() == null || Boolean.TRUE.equals(vm.isActive()))
                .build();

        Analyzer created = analyzerService.create(toCreate);
        AnalyzerResponseVM body = AnalyzerResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/analyzer/" + created.getId()))
                .body(body);
    }

    // ====================== UPDATE ======================
    @PutMapping("/analyzer/{id}")
    public ResponseEntity<AnalyzerResponseVM> updateAnalyzer(
            @PathVariable Long id,
            @Valid @RequestBody AnalyzerUpdateVM vm
    ) {
        LOG.debug("REST update Analyzer id={} payload={}", id, vm);

        Analyzer patch = new Analyzer();
        patch.setCode(vm.code());
        patch.setType(vm.type());
        patch.setIsActive(vm.isActive());

        return analyzerService.update(id, patch)
                .map(AnalyzerResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ====================== READ (Paged) ======================
    @GetMapping("/analyzer")
    public ResponseEntity<List<AnalyzerResponseVM>> getAllAnalyzersPaged(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Analyzers pageable={}", pageable);
        final Page<Analyzer> page = analyzerService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<AnalyzerResponseVM> body = page.getContent()
                .stream()
                .map(AnalyzerResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    // ====================== FILTERS ======================
    @GetMapping("/analyzer/by-type/{type}")
    public ResponseEntity<List<AnalyzerResponseVM>> getByType(
            @PathVariable String type,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Analyzers by type={} pageable={}", type, pageable);
        Page<Analyzer> page = analyzerService.findByType(type, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<AnalyzerResponseVM> body = page.getContent()
                .stream()
                .map(AnalyzerResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/analyzer/by-code/{code}")
    public ResponseEntity<List<AnalyzerResponseVM>> getByCode(
            @PathVariable String code,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Analyzers by code='{}' pageable={}", code, pageable);
        Page<Analyzer> page = analyzerService.findByCodeContainingIgnoreCase(code, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<AnalyzerResponseVM> body = page.getContent()
                .stream()
                .map(AnalyzerResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    // ====================== READ ONE ======================
    @GetMapping("/analyzer/{id}")
    public ResponseEntity<AnalyzerResponseVM> getAnalyzer(@PathVariable Long id) {
        LOG.debug("REST get Analyzer id={}", id);
        return analyzerService.findOne(id)
                .map(AnalyzerResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ====================== DELETE ======================
    @DeleteMapping("/analyzer/{id}")
    public ResponseEntity<Void> deleteAnalyzer(@PathVariable Long id) {
        LOG.debug("REST delete Analyzer id={}", id);
        analyzerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ======================  TOGGLE ACTIVE ======================
    @PatchMapping("/analyzer/{id}/toggle-active")
    public ResponseEntity<AnalyzerResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle Analyzer isActive id={}", id);
        return analyzerService.toggleIsActive(id)
                .map(AnalyzerResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/analyzer/active")
    public ResponseEntity<List<AnalyzerResponseVM>> getActiveAnalyzers(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list active Analyzers pageable={}", pageable);
        Page<Analyzer> page = analyzerService.findActive(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<AnalyzerResponseVM> body = page.getContent()
                .stream()
                .map(AnalyzerResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}
