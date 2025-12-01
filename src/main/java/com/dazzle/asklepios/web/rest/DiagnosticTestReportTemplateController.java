package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.DiagnosticTestReportTemplateService;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestTemplateResponseVM;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestTemplateSaveVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/setup")
public class DiagnosticTestReportTemplateController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestReportTemplateController.class);

    private final DiagnosticTestReportTemplateService service;

    public DiagnosticTestReportTemplateController(DiagnosticTestReportTemplateService service) {
        this.service = service;
    }

    // -------- CREATE --------
    @PostMapping("/diagnostic-test/template")
    public ResponseEntity<DiagnosticTestTemplateResponseVM> create(@RequestBody DiagnosticTestTemplateSaveVM vm) {
        LOG.debug("REST create DiagnosticTestReportTemplate payload={}", vm);
        var created = service.create(vm);
        return ResponseEntity.ok(DiagnosticTestTemplateResponseVM.ofEntity(created));
    }

    // -------- UPDATE by testId --------
    @PutMapping("/diagnostic-test/{testId}/template")
    public ResponseEntity<DiagnosticTestTemplateResponseVM> update(
            @PathVariable Long testId,
            @RequestBody DiagnosticTestTemplateSaveVM vm
    ) {
        LOG.debug("REST update DiagnosticTestReportTemplate testId={} payload={}", testId, vm);
        var updated = service.update(testId, vm);
        return ResponseEntity.ok(DiagnosticTestTemplateResponseVM.ofEntity(updated));
    }

    @GetMapping("/diagnostic-test/{testId}/template")
    public ResponseEntity<DiagnosticTestTemplateResponseVM> getByTest(@PathVariable Long testId) {
        return service.findByTestId(testId)
                .map(DiagnosticTestTemplateResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @DeleteMapping("/diagnostic-test/template/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/diagnostic-test/{testId}/template/assign/{templateId}")
    public ResponseEntity<DiagnosticTestTemplateResponseVM> assign(
            @PathVariable Long testId,
            @PathVariable Long templateId
    ) {
        var saved = service.assignFromLibrary(testId, templateId);
        return ResponseEntity.ok(DiagnosticTestTemplateResponseVM.ofEntity(saved));
    }
}
