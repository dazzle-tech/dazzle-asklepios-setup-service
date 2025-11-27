package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ReportTemplate;
import com.dazzle.asklepios.service.ReportTemplateService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.reporttemplate.ReportTemplateResponseVM;
import com.dazzle.asklepios.web.rest.vm.reporttemplate.ReportTemplateSaveVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class ReportTemplateController {

    private static final Logger LOG = LoggerFactory.getLogger(ReportTemplateController.class);

    private final ReportTemplateService service;

    public ReportTemplateController(ReportTemplateService service) {
        this.service = service;
    }

    /**
     * Create or update template (same endpoint).
     */
    @PostMapping("/report-template")
    public ResponseEntity<ReportTemplateResponseVM> save(@RequestBody ReportTemplateSaveVM vm) {
        LOG.debug("REST save ReportTemplate payload={}", vm);
        ReportTemplate saved = service.save(vm);
        return ResponseEntity.created(URI.create("/api/setup/report-template/" + saved.getId()))
                .body(ReportTemplateResponseVM.ofEntity(saved));
    }

    /**
     * List all templates (paginated).
     */
    @GetMapping("/report-template")
    public ResponseEntity<List<ReportTemplateResponseVM>> list(@ParameterObject Pageable pageable) {
        LOG.debug("REST list ReportTemplates page={}", pageable);
        Page<ReportTemplate> page = service.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(ReportTemplateResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    /**
     * List active templates only.
     */
    @GetMapping("/report-template/active")
    public ResponseEntity<List<ReportTemplateResponseVM>> listActive(@ParameterObject Pageable pageable) {
        LOG.debug("REST list active ReportTemplates page={}", pageable);
        Page<ReportTemplate> page = service.findAllActive(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(ReportTemplateResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    /**
     * Get one template.
     */
    @GetMapping("/report-template/{id}")
    public ResponseEntity<ReportTemplateResponseVM> get(@PathVariable Long id) {
        return service.findOne(id)
                .map(ReportTemplateResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Search by name.
     */
    @GetMapping("/report-template/by-name/{name}")
    public ResponseEntity<List<ReportTemplateResponseVM>> findByName(
            @PathVariable String name,
            @ParameterObject Pageable pageable
    ) {
        Page<ReportTemplate> page = service.findByName(name, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(ReportTemplateResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @PatchMapping("/report-template/{id}/toggle-active")
    public ResponseEntity<ReportTemplateResponseVM> toggleActive(@PathVariable Long id) {
        return service.toggleIsActive(id)
                .map(ReportTemplateResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/report-template/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
