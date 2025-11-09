package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ProcedureCoding;
import com.dazzle.asklepios.domain.enumeration.MedicalCodeType;
import com.dazzle.asklepios.service.ProcedureCodingService;
import com.dazzle.asklepios.service.ProcedureCodingService.CodeOptionDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.procedureCoding.ProcedureCodingCreateVM;
import com.dazzle.asklepios.web.rest.vm.procedureCoding.ProcedureCodingResponseVM;
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
public class ProcedureCodingController {

    private static final Logger LOG = LoggerFactory.getLogger(ProcedureCodingController.class);

    private final ProcedureCodingService service;

    public ProcedureCodingController(ProcedureCodingService service) {
        this.service = service;
    }

    @PostMapping("/procedure-coding")
    public ResponseEntity<ProcedureCodingResponseVM> createProcedureCoding(
            @RequestParam Long procedureId,
            @Valid @RequestBody ProcedureCodingCreateVM vm
    ) {
        LOG.debug("REST create ProcedureCoding procedureId={} payload={}", procedureId, vm);

        ProcedureCoding toCreate = ProcedureCoding.builder()
                .codeType(vm.codeType())
                .codeId(vm.codeId())
                .build();

        ProcedureCoding created = service.create(procedureId, toCreate);
        ProcedureCodingResponseVM body = ProcedureCodingResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/procedure-coding/" + created.getId()))
                .body(body);
    }

    @GetMapping("/procedure-coding/by-procedure/{procedureId}")
    public ResponseEntity<List<ProcedureCodingResponseVM>> getByProcedureId(
            @PathVariable Long procedureId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list ProcedureCoding by procedureId={} pageable={}", procedureId, pageable);
        Page<ProcedureCoding> page = service.findByProcedureId(procedureId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(ProcedureCodingResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/procedure-coding/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete ProcedureCoding id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/procedure-coding/codes")
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
