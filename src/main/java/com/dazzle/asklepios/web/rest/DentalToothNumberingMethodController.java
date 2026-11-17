package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DentalToothNumberingMethod;
import com.dazzle.asklepios.service.DentalToothNumberingMethodService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.dentaltoothnumberingmethod.DentalToothNumberingMethodCreateVM;
import com.dazzle.asklepios.web.rest.vm.dentaltoothnumberingmethod.DentalToothNumberingMethodResponseVM;
import com.dazzle.asklepios.web.rest.vm.dentaltoothnumberingmethod.DentalToothNumberingMethodUpdateVM;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST controller for Dental Tooth Numbering Setup. There is no delete endpoint:
 * methods are referenced by chart rules and chart snapshots, so they are deactivated.
 */
@RestController
@RequestMapping("/api/setup/dental-tooth-numbering-methods")
public class DentalToothNumberingMethodController {

    private static final Logger LOG = LoggerFactory.getLogger(DentalToothNumberingMethodController.class);

    private final DentalToothNumberingMethodService service;

    public DentalToothNumberingMethodController(DentalToothNumberingMethodService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DentalToothNumberingMethodResponseVM> create(
            @Valid @RequestBody DentalToothNumberingMethodCreateVM vm
    ) {
        LOG.debug("REST create DentalToothNumberingMethod name={}", vm.name());
        DentalToothNumberingMethod saved = service.create(vm.toEntity());
        return ResponseEntity
                .created(URI.create("/api/setup/dental-tooth-numbering-methods/" + saved.getId()))
                .body(DentalToothNumberingMethodResponseVM.fromEntity(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DentalToothNumberingMethodResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody DentalToothNumberingMethodUpdateVM vm
    ) {
        LOG.debug("REST update DentalToothNumberingMethod id={}", id);
        return service.update(id, vm.toEntity())
                .map(DentalToothNumberingMethodResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<DentalToothNumberingMethodResponseVM>> findAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST list DentalToothNumberingMethod page={}", pageable);
        return page(service.findAll(pageable));
    }

    /** Active methods for selectors (Dental Chart Setup). */
    @GetMapping("/active")
    public ResponseEntity<List<DentalToothNumberingMethodResponseVM>> findActive() {
        LOG.debug("REST list active DentalToothNumberingMethod");
        return ResponseEntity.ok(
                service.findActive().stream().map(DentalToothNumberingMethodResponseVM::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DentalToothNumberingMethodResponseVM> findOne(@PathVariable Long id) {
        LOG.debug("REST get DentalToothNumberingMethod id={}", id);
        return service.findOne(id)
                .map(DentalToothNumberingMethodResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<List<DentalToothNumberingMethodResponseVM>> findByName(
            @PathVariable String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list DentalToothNumberingMethod by name='{}' page={}", name, pageable);
        return page(service.findByName(name, pageable));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<DentalToothNumberingMethodResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle DentalToothNumberingMethod active id={}", id);
        return service.toggleActive(id)
                .map(DentalToothNumberingMethodResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<List<DentalToothNumberingMethodResponseVM>> page(Page<DentalToothNumberingMethod> page) {
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(DentalToothNumberingMethodResponseVM::fromEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }
}
