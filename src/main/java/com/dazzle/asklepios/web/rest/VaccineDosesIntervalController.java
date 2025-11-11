package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.VaccineDoses;
import com.dazzle.asklepios.domain.VaccineDosesInterval;
import com.dazzle.asklepios.service.VaccineDosesIntervalService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.vaccineDoses.VaccineDosesResponseVM;
import com.dazzle.asklepios.web.rest.vm.vaccineDosesInterval.VaccineDosesIntervalCreateVM;
import com.dazzle.asklepios.web.rest.vm.vaccineDosesInterval.VaccineDosesIntervalResponseVM;
import com.dazzle.asklepios.web.rest.vm.vaccineDosesInterval.VaccineDosesIntervalUpdateVM;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/setup")
public class VaccineDosesIntervalController {

    private static final Logger LOG = LoggerFactory.getLogger(VaccineDosesIntervalController.class);

    private final VaccineDosesIntervalService vaccineDosesIntervalService;

    public VaccineDosesIntervalController(VaccineDosesIntervalService vaccineDosesIntervalService) {
        this.vaccineDosesIntervalService = vaccineDosesIntervalService;
    }

    @PostMapping("/vaccine/{vaccineId}/doses-intervals")
    public ResponseEntity<VaccineDosesIntervalResponseVM> createInterval(
            @PathVariable Long vaccineId,
            @Valid @RequestBody VaccineDosesIntervalCreateVM vm
    ) {
        LOG.debug("REST create VaccineDosesInterval vaccineId={} payload={}", vaccineId, vm);

        VaccineDosesInterval toCreate = VaccineDosesInterval.builder()
                .fromDose(VaccineDoses.builder().id(vm.fromDoseId()).build())
                .toDose(VaccineDoses.builder().id(vm.toDoseId()).build())
                .intervalBetweenDoses(vm.intervalBetweenDoses())
                .unit(vm.unit())
                .isActive(Boolean.TRUE.equals(vm.isActive()))
                .build();

        VaccineDosesInterval created = vaccineDosesIntervalService.create(vaccineId, toCreate);
        VaccineDosesIntervalResponseVM body = VaccineDosesIntervalResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/vaccine-doses-interval/" + created.getId()))
                .body(body);
    }

    @PutMapping("/vaccine-doses-interval/{id}")
    public ResponseEntity<VaccineDosesIntervalResponseVM> updateInterval(
            @PathVariable Long id,
            @Valid @RequestBody VaccineDosesIntervalUpdateVM vm
    ) {
        LOG.debug("REST update VaccineDosesInterval id={} payload={}", id, vm);

        VaccineDosesInterval patch = VaccineDosesInterval.builder()
                .fromDose(VaccineDoses.builder().id(vm.fromDoseId()).build())
                .toDose(VaccineDoses.builder().id(vm.toDoseId()).build())
                .intervalBetweenDoses(vm.intervalBetweenDoses())
                .unit(vm.unit())
                .isActive(vm.isActive())
                .build();

        return vaccineDosesIntervalService.update(id, null, patch)
                .map(VaccineDosesIntervalResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/vaccine/{vaccineId}/doses-intervals")
    public ResponseEntity<List<VaccineDosesIntervalResponseVM>> getIntervalsByVaccine(
            @PathVariable Long vaccineId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list VaccineDosesInterval by vaccineId={} pageable={}", vaccineId, pageable);
        final Page<VaccineDosesInterval> page = vaccineDosesIntervalService.findByVaccineId(vaccineId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(VaccineDosesIntervalResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @PatchMapping("/vaccine-doses-interval/{id}/toggle-active")
    public ResponseEntity<VaccineDosesIntervalResponseVM> toggleIntervalActiveStatus(@PathVariable Long id) {
        LOG.debug("REST toggle VaccineDosesInterval isActive id={}", id);
        return vaccineDosesIntervalService.toggleIsActive(id)
                .map(VaccineDosesIntervalResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/vaccine/{vaccineId}/doses/{fromDoseId}/next")
    @Transactional(readOnly = true)
    public ResponseEntity<List<VaccineDosesResponseVM>> listDosesExcludingFirst(
            @PathVariable Long vaccineId,
            @PathVariable Long fromDoseId
    ) {
        LOG.debug("REST list doses excluding first for vaccineId={} fromDoseId={}", vaccineId, fromDoseId);
        List<VaccineDoses> result = vaccineDosesIntervalService.findDosesByVaccineExcludingFirst(vaccineId, fromDoseId);
        return ResponseEntity.ok(result.stream().map(VaccineDosesResponseVM::ofEntity).toList());
    }
}
