package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.PrescriptionInstruction;
import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import com.dazzle.asklepios.domain.enumeration.MedFrequency;
import com.dazzle.asklepios.domain.enumeration.MedRoa;
import com.dazzle.asklepios.domain.enumeration.UOM;
import com.dazzle.asklepios.service.PrescriptionInstructionService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class PrescriptionInstructionController {

    private static final Logger LOG = LoggerFactory.getLogger(PrescriptionInstructionController.class);
    private final PrescriptionInstructionService service;

    public PrescriptionInstructionController(PrescriptionInstructionService service) {
        this.service = service;
    }

    @PostMapping("/prescription-instruction")
    public ResponseEntity<PrescriptionInstruction> create(@Valid @RequestBody PrescriptionInstruction body) {
        LOG.debug("REST create PrescriptionInstruction payload={}", body);
        PrescriptionInstruction saved = service.create(body);
        return ResponseEntity
                .created(URI.create("/api/setup/prescription-instruction/" + saved.getId()))
                .body(saved);
    }

    @PutMapping("/prescription-instruction/{id}")
    public ResponseEntity<PrescriptionInstruction> update(@PathVariable Long id,
                                                          @Valid @RequestBody PrescriptionInstruction body) {
        LOG.debug("REST update PrescriptionInstruction id={} payload={}", id, body);
        return service.update(id, body)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** GET all (paged) */
    @GetMapping("/prescription-instruction")
    public ResponseEntity<List<PrescriptionInstruction>> getAll(@ParameterObject Pageable pageable) {
        Page<PrescriptionInstruction> page = service.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /** GET by category (paged) */
    @GetMapping("/prescription-instruction/by-category/{category}")
    public ResponseEntity<List<PrescriptionInstruction>> byCategory(@PathVariable AgeGroupType category,
                                                                    @ParameterObject Pageable pageable) {
        Page<PrescriptionInstruction> page = service.findByCategory(category, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /** GET by unit (paged) */
    @GetMapping("/prescription-instruction/by-unit/{unit}")
    public ResponseEntity<List<PrescriptionInstruction>> byUnit(@PathVariable UOM unit,
                                                                @ParameterObject Pageable pageable) {
        Page<PrescriptionInstruction> page = service.findByUnit(unit, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /** GET by route (paged) */
    @GetMapping("/prescription-instruction/by-route/{route}")
    public ResponseEntity<List<PrescriptionInstruction>> byRoute(@PathVariable MedRoa route,
                                                                 @ParameterObject Pageable pageable) {
        Page<PrescriptionInstruction> page = service.findByRoute(route, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /** GET by frequency (paged) */
    @GetMapping("/prescription-instruction/by-frequency/{frequency}")
    public ResponseEntity<List<PrescriptionInstruction>> byFrequency(@PathVariable MedFrequency frequency,
                                                                     @ParameterObject Pageable pageable) {
        Page<PrescriptionInstruction> page = service.findByFrequency(frequency, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/prescription-instruction/{id}")
    public ResponseEntity<PrescriptionInstruction> get(@PathVariable Long id) {
        return service.findOne(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/prescription-instruction/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
