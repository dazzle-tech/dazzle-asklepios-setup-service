package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.LoincCode;
import com.dazzle.asklepios.domain.enumeration.LoincCategory;
import com.dazzle.asklepios.repository.LoincCodeRepository;
import com.dazzle.asklepios.service.LoincCodeService;
import com.dazzle.asklepios.service.dto.LoincImportResultDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.loinc.LoincCodeCreateVM;
import com.dazzle.asklepios.web.rest.vm.loinc.LoincCodeUpdateVM;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class LoincCodeController {

    private final LoincCodeService service;
    private final LoincCodeRepository repository;

    @PostMapping("/loinc/import")
    public ResponseEntity<LoincImportResultDTO> importLoinc(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite
    ) {
        LoincImportResultDTO result = service.importCsv(file, overwrite);
        if (!overwrite && !result.conflicts().isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/loinc")
    public ResponseEntity<LoincCode> create(@Valid @RequestBody LoincCodeCreateVM vm) {
        LoincCode created = service.create(vm.code(), vm.category(), vm.description());
        return ResponseEntity
                .created(URI.create("/api/setup/loinc/" + created.getId()))
                .body(created);
    }

    @PutMapping("/loinc/{id:\\d+}")
    public ResponseEntity<LoincCode> update(@PathVariable Long id, @Valid @RequestBody LoincCodeUpdateVM vm) {
        return service.update(id, vm.code(), vm.category(), vm.description())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/loinc/all")
    public ResponseEntity<List<LoincCode>> getAll(@ParameterObject Pageable pageable) {
        Page<LoincCode> page = repository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/loinc/{id:\\d+}")
    public ResponseEntity<LoincCode> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ====================== FILTERS ======================

    @GetMapping("/loinc/by-category/{category}")
    public ResponseEntity<List<LoincCode>> getByCategory(
            @PathVariable LoincCategory category,
            @ParameterObject Pageable pageable
    ) {
        Page<LoincCode> page = repository.findByCategory(category, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/loinc/by-code/{code}")
    public ResponseEntity<List<LoincCode>> getByCode(
            @PathVariable String code,
            @ParameterObject Pageable pageable
    ) {
        Page<LoincCode> page = repository.findByCodeContainingIgnoreCase(code, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/loinc/by-description/{description}")
    public ResponseEntity<List<LoincCode>> getByDescription(
            @PathVariable String description,
            @ParameterObject Pageable pageable
    ) {
        Page<LoincCode> page = repository.findByDescriptionContainingIgnoreCase(description, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
