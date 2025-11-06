package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.CdtCode;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.CdtClass;
import com.dazzle.asklepios.repository.CdtCodeRepository;
import com.dazzle.asklepios.service.CdtCodeService;
import com.dazzle.asklepios.service.CdtServiceMappingService;
import com.dazzle.asklepios.service.dto.CdtImportResultDTO;
import com.dazzle.asklepios.service.dto.CdtServiceMappingSyncResultDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class CdtCodeController {

    private final CdtCodeService service;
    private final CdtCodeRepository repository;
    private final CdtServiceMappingService mappingService;


    @PostMapping("/cdt/import")
    public ResponseEntity<CdtImportResultDTO> importCdt(
                                                           @RequestParam("file") MultipartFile file,
                                                           @RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite
    ) {
        CdtImportResultDTO result = service.importCsv(file, overwrite);
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

    @GetMapping("cdt/{cdtId}/services")
    public ResponseEntity<List<Long>> getLinked(@PathVariable Long cdtId) {
        return ResponseEntity.ok(mappingService.getLinkedServiceIds(cdtId));
    }

    @GetMapping("cdt/{cdtId}/services/details")
    public ResponseEntity<List<ServiceSetup>> getLinkedDetails(@PathVariable Long cdtId) {
        return ResponseEntity.ok(mappingService.getLinkedServices(cdtId));
    }

    @PutMapping("cdt/{cdtId}/services")
    public ResponseEntity<CdtServiceMappingSyncResultDTO> sync(
            @PathVariable Long cdtId,
            @RequestBody List<Long> serviceIds
    ) {
        return ResponseEntity.ok(mappingService.sync(cdtId, serviceIds));
    }

}
