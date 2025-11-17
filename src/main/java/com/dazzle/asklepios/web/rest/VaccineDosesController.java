package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.VaccineDoses;
import com.dazzle.asklepios.domain.enumeration.DoseNumber;
import com.dazzle.asklepios.domain.enumeration.NumberOfDoses;
import com.dazzle.asklepios.service.VaccineDosesService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;

import com.dazzle.asklepios.web.rest.vm.vaccineDoses.VaccineDosesCreateVM;
import com.dazzle.asklepios.web.rest.vm.vaccineDoses.VaccineDosesResponseVM;
import com.dazzle.asklepios.web.rest.vm.vaccineDoses.VaccineDosesUpdateVM;
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
public class VaccineDosesController {

    private static final Logger LOG = LoggerFactory.getLogger(VaccineDosesController.class);

    private final VaccineDosesService vaccineDosesService;

    public VaccineDosesController(VaccineDosesService vaccineDosesService) {
        this.vaccineDosesService = vaccineDosesService;
    }

    @PostMapping("/vaccine/{vaccineId}/doses")
    public ResponseEntity<VaccineDosesResponseVM> createVaccineDose(
            @PathVariable Long vaccineId,
            @Valid @RequestBody VaccineDosesCreateVM vm
    ) {
        LOG.debug("REST create VaccineDose vaccineId={} payload={}", vaccineId, vm);

        VaccineDoses toCreate = VaccineDoses.builder()
                .doseNumber(vm.doseNumber())
                .fromAge(vm.fromAge())
                .toAge(vm.toAge())
                .fromAgeUnit(vm.fromAgeUnit())
                .toAgeUnit(vm.toAgeUnit())
                .isBooster(Boolean.TRUE.equals(vm.isBooster()))
                .isActive(Boolean.TRUE.equals(vm.isActive()))
                .build();

        VaccineDoses created = vaccineDosesService.create(vaccineId, toCreate);
        VaccineDosesResponseVM body = VaccineDosesResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/vaccine-doses/" + created.getId()))
                .body(body);
    }

    @PutMapping("/vaccine-doses/{id}")
    public ResponseEntity<VaccineDosesResponseVM> updateVaccineDose(
            @PathVariable Long id,
            @Valid @RequestBody VaccineDosesUpdateVM vm
    ) {
        LOG.debug("REST update VaccineDose id={} payload={}", id, vm);

        VaccineDoses patch = new VaccineDoses();
        patch.setDoseNumber(vm.doseNumber());
        patch.setFromAge(vm.fromAge());
        patch.setToAge(vm.toAge());
        patch.setFromAgeUnit(vm.fromAgeUnit());
        patch.setToAgeUnit(vm.toAgeUnit());
        patch.setIsBooster(vm.isBooster());
        patch.setIsActive(vm.isActive());

        return vaccineDosesService.update(id, null, patch)
                .map(VaccineDosesResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/vaccine/{vaccineId}/doses")
    public ResponseEntity<List<VaccineDosesResponseVM>> getByVaccine(
            @PathVariable Long vaccineId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list VaccineDoses by vaccineId={} pageable={}", vaccineId, pageable);
        final Page<VaccineDoses> page = vaccineDosesService.findByVaccineId(vaccineId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(VaccineDosesResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @PatchMapping("/vaccine-doses/{id}/toggle-active")
    public ResponseEntity<VaccineDosesResponseVM> toggleVaccineDoseActiveStatus(@PathVariable Long id) {
        LOG.debug("REST toggle VaccineDose isActive id={}", id);
        return vaccineDosesService.toggleIsActive(id)
                .map(VaccineDosesResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/vaccine-doses/dose-numbers/up-to/{numberOfDoses}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DoseNumber>> listFirstDoseNumbers(@PathVariable NumberOfDoses numberOfDoses) {
        LOG.debug("REST list first DoseNumbers up to {}", numberOfDoses);
        List<DoseNumber> result = vaccineDosesService.getDoseNumbersUpTo(numberOfDoses);
        return ResponseEntity.ok(result);
    }
}
