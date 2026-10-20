package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.MedFrequencySetup;
import com.dazzle.asklepios.domain.enumeration.FrequencyList;
import com.dazzle.asklepios.domain.enumeration.MedFrequency;
import com.dazzle.asklepios.service.MedFrequencySetupService;
import com.dazzle.asklepios.service.dto.medFrequencySetup.MedFrequencySetupCreateDTO;
import com.dazzle.asklepios.service.dto.medFrequencySetup.MedFrequencySetupUpdateDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.medFrequencySetup.MedFrequencySetupResponseVM;
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

/** Setup of how each MedFrequency value is split into doses */
@RestController
@RequestMapping("/api/setup")
public class MedFrequencySetupController {

    private static final Logger LOG = LoggerFactory.getLogger(MedFrequencySetupController.class);
    private final MedFrequencySetupService service;

    public MedFrequencySetupController(MedFrequencySetupService service) {
        this.service = service;
    }

    @PostMapping("/med-frequency-setup")
    public ResponseEntity<MedFrequencySetupResponseVM> create(@Valid @RequestBody MedFrequencySetupCreateDTO dto) {
        LOG.debug("REST create MedFrequencySetup payload={}", dto);
        MedFrequencySetup saved = service.create(dto);
        return ResponseEntity
                .created(URI.create("/api/setup/med-frequency-setup/" + saved.getId()))
                .body(MedFrequencySetupResponseVM.ofEntity(saved));
    }

    @PutMapping("/med-frequency-setup/{id}")
    public ResponseEntity<MedFrequencySetupResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody MedFrequencySetupUpdateDTO dto
    ) {
        LOG.debug("REST update MedFrequencySetup id={} payload={}", id, dto);
        return ResponseEntity.ok(MedFrequencySetupResponseVM.ofEntity(service.update(id, dto)));
    }

    @PatchMapping("/med-frequency-setup/{id}/toggle-active")
    public ResponseEntity<MedFrequencySetupResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle MedFrequencySetup isActive id={}", id);
        return ResponseEntity.ok(MedFrequencySetupResponseVM.ofEntity(service.toggleIsActive(id)));
    }

    /** GET all (paged) */
    @GetMapping("/med-frequency-setup")
    public ResponseEntity<List<MedFrequencySetupResponseVM>> getAll(@ParameterObject Pageable pageable) {
        Page<MedFrequencySetup> page = service.findAll(pageable);
        return paged(page);
    }

    /** GET by status (paged) */
    @GetMapping("/med-frequency-setup/by-status/{isActive}")
    public ResponseEntity<List<MedFrequencySetupResponseVM>> getByStatus(
            @PathVariable Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        return paged(service.findByStatus(isActive, pageable));
    }

    /** GET by frequency number (paged) */
    @GetMapping("/med-frequency-setup/by-frequency-number/{frequencyNumber}")
    public ResponseEntity<List<MedFrequencySetupResponseVM>> getByFrequencyNumber(
            @PathVariable Integer frequencyNumber,
            @ParameterObject Pageable pageable
    ) {
        return paged(service.findByFrequencyNumber(frequencyNumber, pageable));
    }

    /** GET by frequency unit (paged) */
    @GetMapping("/med-frequency-setup/by-frequency-unit/{frequencyUnit}")
    public ResponseEntity<List<MedFrequencySetupResponseVM>> getByFrequencyUnit(
            @PathVariable FrequencyList frequencyUnit,
            @ParameterObject Pageable pageable
    ) {
        return paged(service.findByFrequencyUnit(frequencyUnit, pageable));
    }

    /** GET the setup of one frequency (404 when the frequency has no setup) */
    @GetMapping("/med-frequency-setup/by-frequency/{frequency}")
    public ResponseEntity<MedFrequencySetupResponseVM> getByFrequency(@PathVariable MedFrequency frequency) {
        return service.findByFrequency(frequency)
                .map(MedFrequencySetupResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static ResponseEntity<List<MedFrequencySetupResponseVM>> paged(Page<MedFrequencySetup> page) {
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(MedFrequencySetupResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }
}
