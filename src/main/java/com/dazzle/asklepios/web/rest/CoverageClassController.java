package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.CoverageClassService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageClassResponseVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageClassSaveVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageClassUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class CoverageClassController {

    private static final Logger LOG = LoggerFactory.getLogger(CoverageClassController.class);

    private final CoverageClassService coverageClassService;

    public CoverageClassController(CoverageClassService coverageClassService) {
        this.coverageClassService = coverageClassService;
    }

    @PostMapping("/coverage-classes")
    public ResponseEntity<CoverageClassResponseVM> create(@Valid @RequestBody CoverageClassSaveVM vm) {
        LOG.debug("REST create coverage class payload={}", vm);
        CoverageClassResponseVM body = coverageClassService.create(vm);
        return ResponseEntity.created(URI.create("/api/setup/coverage-classes/" + body.id())).body(body);
    }

    @PutMapping("/coverage-classes")
    public ResponseEntity<CoverageClassResponseVM> update(@Valid @RequestBody CoverageClassUpdateVM vm) {
        LOG.debug("REST update coverage class payload={}", vm);
        return ResponseEntity.ok(coverageClassService.update(vm));
    }

    @GetMapping("/coverage-classes/{id:\\d+}")
    public ResponseEntity<CoverageClassResponseVM> get(@PathVariable Long id) {
        return ResponseEntity.ok(coverageClassService.get(id));
    }

    @PatchMapping("/coverage-classes/{id:\\d+}/toggle-active")
    public ResponseEntity<CoverageClassResponseVM> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(coverageClassService.toggleActive(id));
    }

    @GetMapping("/coverage-contracts/{contractId:\\d+}/classes")
    public ResponseEntity<List<CoverageClassResponseVM>> list(
            @PathVariable Long contractId,
            @RequestParam(required = false) Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageClassService.list(contractId, isActive, pageable));
    }

    private <T> ResponseEntity<List<T>> paged(Page<T> page) {
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
