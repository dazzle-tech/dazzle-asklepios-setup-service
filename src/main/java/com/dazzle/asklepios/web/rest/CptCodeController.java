package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.CptCode;
import com.dazzle.asklepios.domain.enumeration.CptCategory;
import com.dazzle.asklepios.repository.CptCodeRepository;
import com.dazzle.asklepios.service.CptCodeService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class CptCodeController {

    private final CptCodeService service;
    private final CptCodeRepository repository;

    @PostMapping("/cpt/import")
    public ResponseEntity<?> importCpt(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite
    ) {
        CptCodeService.ImportResult result = service.importCsv(file, overwrite);
        if (!overwrite && !result.conflicts().isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cpt/all")
    public ResponseEntity<List<CptCode>> getAll(@ParameterObject Pageable pageable) {
        Page<CptCode> page = repository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    // ====================== FILTERS ======================

    @GetMapping("/cpt/by-category/{category}")
    public ResponseEntity<List<CptCode>> getByCategory(
            @PathVariable CptCategory category,
            @ParameterObject Pageable pageable
    ) {
        Page<CptCode> page = repository.findByCategory(category, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/cpt/by-code/{code}")
    public ResponseEntity<List<CptCode>> getByCode(
            @PathVariable String code,
            @ParameterObject Pageable pageable
    ) {
        Page<CptCode> page = repository.findByCodeContainingIgnoreCase(code, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/cpt/by-description/{description}")
    public ResponseEntity<List<CptCode>> getByDescription(
            @PathVariable String description,
            @ParameterObject Pageable pageable
    ) {
        Page<CptCode> page = repository.findByDescriptionContainingIgnoreCase(description, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
