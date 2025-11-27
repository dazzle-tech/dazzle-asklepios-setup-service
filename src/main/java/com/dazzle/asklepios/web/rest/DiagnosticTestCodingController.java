package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticTestCoding;
import com.dazzle.asklepios.domain.enumeration.MedicalCodeType;
import com.dazzle.asklepios.service.DiagnosticTestCodingService;
import com.dazzle.asklepios.service.DiagnosticTestCodingService.CodeOptionDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.diagnosticTestCoding.DiagnosticTestCodingCreateVM;
import com.dazzle.asklepios.web.rest.vm.diagnosticTestCoding.DiagnosticTestCodingResponseVM;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class DiagnosticTestCodingController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestCodingController.class);

    private final DiagnosticTestCodingService service;

    public DiagnosticTestCodingController(DiagnosticTestCodingService service) {
        this.service = service;
    }

    @PostMapping("/diagnostic-test-coding")
    public ResponseEntity<DiagnosticTestCodingResponseVM> createDiagnosticTestCoding(
            @RequestParam Long diagnosticTestId,
            @Valid @RequestBody DiagnosticTestCodingCreateVM vm
    ) {
        LOG.debug("REST create DiagnosticTestCoding diagnosticTestId={} payload={}", diagnosticTestId, vm);

        DiagnosticTestCoding toCreate = DiagnosticTestCoding.builder()
                .codeType(vm.codeType())
                .codeId(vm.codeId())
                .build();

        DiagnosticTestCoding created = service.create(diagnosticTestId, toCreate);
        DiagnosticTestCodingResponseVM body = DiagnosticTestCodingResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/diagnostic-test-coding/" + created.getId()))
                .body(body);
    }

    @GetMapping("/diagnostic-test-coding/by-diagnostic-test/{diagnosticTestId}")
    public ResponseEntity<List<DiagnosticTestCodingResponseVM>> getByDiagnosticTestId(
            @PathVariable Long diagnosticTestId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list DiagnosticTestCoding by diagnosticTestId={} pageable={}", diagnosticTestId, pageable);
        Page<DiagnosticTestCoding> page = service.findByDiagnosticTestId(diagnosticTestId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(DiagnosticTestCodingResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/diagnostic-test-coding/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete DiagnosticTestCoding id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/diagnostic-test-coding/codes")
    public ResponseEntity<List<CodeOptionVM>> getCodesByType(
            @RequestParam MedicalCodeType type,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list codes by type={} pageable={}", type, pageable);
        Page<CodeOptionDTO> page = service.findCodesByType(type, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                page.getContent().stream()
                        .map(c -> new CodeOptionVM(c.id(), c.code(), c.description()))
                        .toList(),
                headers,
                HttpStatus.OK
        );
    }

    public record CodeOptionVM(Long id, String code, String description) {}
}
