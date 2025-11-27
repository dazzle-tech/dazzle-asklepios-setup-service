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

    /**
     * Create or update (same endpoint).
     */
    @PostMapping("/diagnostic-test/template")
    public ResponseEntity<DiagnosticTestTemplateResponseVM> save(@RequestBody DiagnosticTestTemplateSaveVM vm) {
        LOG.debug("REST request to save DiagnosticTestReportTemplate payload={}", vm);
        var saved = service.save(vm);
        return ResponseEntity.ok(DiagnosticTestTemplateResponseVM.ofEntity(saved));
    }

    /**
     * Get template by test id.
     */
    @GetMapping("/diagnostic-test/{testId}/template")
    public ResponseEntity<DiagnosticTestTemplateResponseVM> getByTest(@PathVariable Long testId) {
        LOG.debug("REST request to get DiagnosticTestReportTemplate testId={}", testId);
        return service.findByTestId(testId)
                .map(DiagnosticTestTemplateResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Optional delete template (if you want).
     */
    @DeleteMapping("/diagnostic-test/template/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete DiagnosticTestReportTemplate id={}", id);
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
