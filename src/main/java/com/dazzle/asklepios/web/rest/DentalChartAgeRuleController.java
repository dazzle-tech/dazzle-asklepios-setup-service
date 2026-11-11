package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DentalChartAgeRule;
import com.dazzle.asklepios.domain.enumeration.DentalChartType;
import com.dazzle.asklepios.service.DentalChartAgeRuleService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.dentalchartagerule.DentalChartAgeRuleCreateVM;
import com.dazzle.asklepios.web.rest.vm.dentalchartagerule.DentalChartAgeRuleResponseVM;
import com.dazzle.asklepios.web.rest.vm.dentalchartagerule.DentalChartAgeRuleUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * REST controller for Dental Chart Setup age-range configuration.
 */
@RestController
@RequestMapping("/api/setup/dental-chart-age-rules")
public class DentalChartAgeRuleController {

    private static final Logger LOG = LoggerFactory.getLogger(DentalChartAgeRuleController.class);

    private final DentalChartAgeRuleService service;

    public DentalChartAgeRuleController(DentalChartAgeRuleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DentalChartAgeRuleResponseVM> create(
            @Valid @RequestBody DentalChartAgeRuleCreateVM vm
    ) {
        LOG.debug("REST create DentalChartAgeRule payload={}", vm);
        DentalChartAgeRule saved = service.create(vm.toEntity());
        return ResponseEntity
                .created(URI.create("/api/setup/dental-chart-age-rules/" + saved.getId()))
                .body(DentalChartAgeRuleResponseVM.fromEntity(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DentalChartAgeRuleResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody DentalChartAgeRuleUpdateVM vm
    ) {
        LOG.debug("REST update DentalChartAgeRule id={} payload={}", id, vm);
        return service.update(id, vm.toEntity())
                .map(DentalChartAgeRuleResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<DentalChartAgeRuleResponseVM>> findAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST list DentalChartAgeRule page={}", pageable);
        Page<DentalChartAgeRule> page = service.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(DentalChartAgeRuleResponseVM::fromEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    /**
     * Resolve the active age rule for a patient birth date as of a reference date
     * (typically the encounter date). Ages use completed years with [ageFrom, ageTo).
     * Declared before /{id} so "resolve" is not captured as a path variable.
     */
    @GetMapping("/resolve")
    public ResponseEntity<DentalChartAgeRuleResponseVM> resolveByBirthDate(
            @RequestParam("birthDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate birthDate,
            @RequestParam(value = "asOfDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate asOfDate
    ) {
        LocalDate reference = asOfDate != null ? asOfDate : LocalDate.now();
        int ageYears = Period.between(birthDate, reference).getYears();
        LOG.debug("REST resolve DentalChartAgeRule birthDate={} asOfDate={} ageYears={}", birthDate, reference, ageYears);
        return service.findActiveRuleForAge(ageYears)
                .map(DentalChartAgeRuleResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DentalChartAgeRuleResponseVM> findOne(@PathVariable Long id) {
        LOG.debug("REST get DentalChartAgeRule id={}", id);
        return service.findOne(id)
                .map(DentalChartAgeRuleResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<List<DentalChartAgeRuleResponseVM>> findByName(
            @PathVariable String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list DentalChartAgeRule by name='{}' page={}", name, pageable);
        Page<DentalChartAgeRule> page = service.findByName(name, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(DentalChartAgeRuleResponseVM::fromEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/by-chart-type/{chartType}")
    public ResponseEntity<List<DentalChartAgeRuleResponseVM>> findByChartType(
            @PathVariable DentalChartType chartType,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list DentalChartAgeRule by chartType={} page={}", chartType, pageable);
        Page<DentalChartAgeRule> page = service.findByChartType(chartType, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(DentalChartAgeRuleResponseVM::fromEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<DentalChartAgeRuleResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle DentalChartAgeRule active id={}", id);
        return service.toggleActive(id)
                .map(DentalChartAgeRuleResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete DentalChartAgeRule id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
