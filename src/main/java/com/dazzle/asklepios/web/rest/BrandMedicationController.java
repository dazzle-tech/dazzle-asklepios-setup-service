package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.service.BrandMedicationService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.brandMedication.BrandMedicationCreateVM;
import com.dazzle.asklepios.web.rest.vm.brandMedication.BrandMedicationResponseVM;
import com.dazzle.asklepios.web.rest.vm.brandMedication.BrandMedicationUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class BrandMedicationController {

    private static final Logger LOG = LoggerFactory.getLogger(BrandMedicationController.class);

    private final BrandMedicationService brandMedicationService;

    public BrandMedicationController(BrandMedicationService brandMedicationService) {
        this.brandMedicationService = brandMedicationService;
    }

    /**
     * {@code POST /brand-medication} : Create a new BrandMedication.
     */
    @PostMapping("/brand-medication")
    public ResponseEntity<BrandMedicationResponseVM> create(@Valid @RequestBody BrandMedicationCreateVM vm) {
        LOG.debug("REST create BrandMedication payload={}", vm);
        BrandMedication created = brandMedicationService.create(vm);
        BrandMedicationResponseVM body = BrandMedicationResponseVM.ofEntity(created);
        LOG.debug("REST create BrandMedication response={}", body);
        return ResponseEntity
                .created(URI.create("/api/setup/brand-medication/" + created.getId()))
                .body(body);
    }

    /**
     * {@code PUT /brand-medication/{id}} : Update an existing BrandMedication.
     */
    @PutMapping("/brand-medication/{id}")
    public ResponseEntity<BrandMedicationResponseVM> update(@PathVariable Long id, @Valid @RequestBody BrandMedicationUpdateVM vm) {
        LOG.debug("REST update BrandMedication id={} payload={}", id, vm);
        return brandMedicationService.update(id, vm)
                .map(BrandMedicationResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /brand-medication} : Get a paginated list of BrandMedications.
     */
    @GetMapping("/brand-medication")
    public ResponseEntity<List<BrandMedicationResponseVM>> getAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST list BrandMedications page={}", pageable);
        Page<BrandMedication> page = brandMedicationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(BrandMedicationResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    /**
     * {@code GET /brand-medication/{id}} : Get a single BrandMedication by id.
     */
    @GetMapping("/brand-medication/{id}")
    public ResponseEntity<BrandMedicationResponseVM> getOne(@PathVariable Long id) {
        LOG.debug("REST get BrandMedication id={}", id);
        return brandMedicationService.findOne(id)
                .map(BrandMedicationResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code PATCH /brand-medication/{id}/toggle-active} : Toggle isActive.
     */
    @PatchMapping("/brand-medication/{id}/toggle-active")
    public ResponseEntity<BrandMedicationResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle BrandMedication isActive id={}", id);
        return brandMedicationService.toggleIsActive(id)
                .map(BrandMedicationResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/brand-medication/by-name/{name}")
    public ResponseEntity<List<BrandMedicationResponseVM>> getByName(@PathVariable String name, @ParameterObject Pageable pageable) {
        LOG.debug("REST list BrandMedications by name='{}' page={}", name, pageable);
        Page<BrandMedication> page = brandMedicationService.findByName(name, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(BrandMedicationResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/brand-medication/by-manufacturer/{manufacturer}")
    public ResponseEntity<List<BrandMedicationResponseVM>> getByManufacturer(@PathVariable String manufacturer, @ParameterObject Pageable pageable) {
        LOG.debug("REST list BrandMedications by manufacturer='{}' page={}", manufacturer, pageable);
        Page<BrandMedication> page = brandMedicationService.findByManufacturer(manufacturer, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(BrandMedicationResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/brand-medication/by-dosage-form/{dosageForm}")
    public ResponseEntity<List<BrandMedicationResponseVM>> getByDosageForm(@PathVariable String dosageForm, @ParameterObject Pageable pageable) {
        LOG.debug("REST list BrandMedications by dosageForm='{}' page={}", dosageForm, pageable);
        Page<BrandMedication> page = brandMedicationService.findByDosageForm(dosageForm, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(BrandMedicationResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/brand-medication/by-usage-instructions/{usageInstructions}")
    public ResponseEntity<List<BrandMedicationResponseVM>> getByUsageInstructions(@PathVariable String usageInstructions, @ParameterObject Pageable pageable) {
        LOG.debug("REST list BrandMedications by usageInstructions like='{}' page={}", usageInstructions, pageable);
        Page<BrandMedication> page = brandMedicationService.findByUsageInstructions(usageInstructions, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(BrandMedicationResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/brand-medication/by-roa/{roa}")
    public ResponseEntity<List<BrandMedicationResponseVM>> getByRoa(@PathVariable String roa, @ParameterObject Pageable pageable) {
        LOG.debug("REST list BrandMedications by roa='{}' page={}", roa, pageable);
        Page<BrandMedication> page = brandMedicationService.findByRoa(roa, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(BrandMedicationResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/brand-medication/by-expires-after-opening/{expiresAfterOpening}")
    public ResponseEntity<List<BrandMedicationResponseVM>> getByExpiresAfterOpening(@PathVariable Boolean expiresAfterOpening, @ParameterObject Pageable pageable) {
        LOG.debug("REST list BrandMedications by expiresAfterOpening={} page={}", expiresAfterOpening, pageable);
        Page<BrandMedication> page = brandMedicationService.findByExpiresAfterOpening(expiresAfterOpening, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(BrandMedicationResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/brand-medication/by-use-single-patient/{useSinglePatient}")
    public ResponseEntity<List<BrandMedicationResponseVM>> getByUseSinglePatient(@PathVariable Boolean useSinglePatient, @ParameterObject Pageable pageable) {
        LOG.debug("REST list BrandMedications by useSinglePatient={} page={}", useSinglePatient, pageable);
        Page<BrandMedication> page = brandMedicationService.findByUseSinglePatient(useSinglePatient, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(BrandMedicationResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/brand-medication/by-is-active/{isActive}")
    public ResponseEntity<List<BrandMedicationResponseVM>> getByIsActive(@PathVariable Boolean isActive, @ParameterObject Pageable pageable) {
        LOG.debug("REST list BrandMedications by isActive={} page={}", isActive, pageable);
        Page<BrandMedication> page = brandMedicationService.findByIsActive(isActive, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(BrandMedicationResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }
}
