package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.CdtCode;
import com.dazzle.asklepios.domain.enumeration.CdtClass;
import com.dazzle.asklepios.repository.CdtCodeRepository;
import com.dazzle.asklepios.service.CdtCodeService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class CdtCodeController {

    private final CdtCodeService service;
    private final CdtCodeRepository repository;

    // ====================== IMPORT ======================

    @PostMapping("/cdt/import")
    public ResponseEntity<?> importCdt(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite
    ) {
        CdtCodeService.ImportResult result = service.importCsv(file, overwrite);
        if (!overwrite && !result.conflicts().isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    // ====================== READ ALL ======================

    @GetMapping("/cdt/all")
    public ResponseEntity<List<CdtCode>> getAll(@ParameterObject Pageable pageable) {
        Page<CdtCode> page = repository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    // ====================== FILTERS ======================

    @GetMapping("/cdt/by-class/{cdtClass}")
    public ResponseEntity<List<CdtCode>> getByClass(
            @PathVariable CdtClass cdtClass,
            @ParameterObject Pageable pageable
    ) {
        Page<CdtCode> page = repository.findByCdtClass(cdtClass, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/cdt/by-active/{active}")
    public ResponseEntity<List<CdtCode>> getByActive(
            @PathVariable boolean active,
            @ParameterObject Pageable pageable
    ) {
        Page<CdtCode> page = repository.findByIsActive(active, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/cdt/by-code/{code}")
    public ResponseEntity<List<CdtCode>> getByCode(
            @PathVariable String code,
            @ParameterObject Pageable pageable
    ) {
        Page<CdtCode> page = repository.findByCodeContainingIgnoreCase(code, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/cdt/by-description/{description}")
    public ResponseEntity<List<CdtCode>> getByDescription(
            @PathVariable String description,
            @ParameterObject Pageable pageable
    ) {
        Page<CdtCode> page = repository.findByDescriptionContainingIgnoreCase(description, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
