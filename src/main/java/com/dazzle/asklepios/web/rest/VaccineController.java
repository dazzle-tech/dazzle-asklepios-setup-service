package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Vaccine;
import com.dazzle.asklepios.domain.enumeration.RouteOfAdministration;
import com.dazzle.asklepios.domain.enumeration.VaccineType;
import com.dazzle.asklepios.service.VaccineService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.vaccine.VaccineCreateVM;
import com.dazzle.asklepios.web.rest.vm.vaccine.VaccineResponseVM;
import com.dazzle.asklepios.web.rest.vm.vaccine.VaccineUpdateVM;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class VaccineController {

    private static final Logger LOG = LoggerFactory.getLogger(VaccineController.class);

    private final VaccineService vaccineService;

    public VaccineController(VaccineService vaccineService) {
        this.vaccineService = vaccineService;
    }

    @PostMapping("/vaccine")
    public ResponseEntity<VaccineResponseVM> createVaccine(
            @Valid @RequestBody VaccineCreateVM vm
    ) {
        LOG.debug("REST create Vaccine payload={}", vm);

        Vaccine toCreate = Vaccine.builder()
                .name(vm.name())
                .type(vm.type())
                .roa(vm.roa())
                .atcCode(vm.atcCode())
                .siteOfAdministration(vm.siteOfAdministration())
                .postOpeningDuration(vm.postOpeningDuration())
                .durationUnit(vm.durationUnit())
                .numberOfDoses(vm.numberOfDoses())
                .indications(vm.indications())
                .possibleReactions(vm.possibleReactions())
                .contraindicationsAndPrecautions(vm.contraindicationsAndPrecautions())
                .storageAndHandling(vm.storageAndHandling())
                .isActive(Boolean.TRUE.equals(vm.isActive()))
                .build();

        Vaccine created = vaccineService.create(toCreate);
        VaccineResponseVM body = VaccineResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/vaccine/" + created.getId()))
                .body(body);
    }

    @PutMapping("/vaccine/{id}")
    public ResponseEntity<VaccineResponseVM> updateVaccine(
            @PathVariable Long id,
            @Valid @RequestBody VaccineUpdateVM vm
    ) {
        LOG.debug("REST update Vaccine id={} payload={}", id, vm);

        Vaccine patch = new Vaccine();
        patch.setName(vm.name());
        patch.setType(vm.type());
        patch.setRoa(vm.roa());
        patch.setAtcCode(vm.atcCode());
        patch.setSiteOfAdministration(vm.siteOfAdministration());
        patch.setPostOpeningDuration(vm.postOpeningDuration());
        patch.setDurationUnit(vm.durationUnit());
        patch.setNumberOfDoses(vm.numberOfDoses());
        patch.setIndications(vm.indications());
        patch.setPossibleReactions(vm.possibleReactions());
        patch.setContraindicationsAndPrecautions(vm.contraindicationsAndPrecautions());
        patch.setStorageAndHandling(vm.storageAndHandling());
        patch.setIsActive(vm.isActive());

        return vaccineService.update(id, patch)
                .map(VaccineResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/vaccine")
    public ResponseEntity<List<VaccineResponseVM>> getAllVaccines(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Vaccines pageable={}", pageable);
        final Page<Vaccine> page = vaccineService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(VaccineResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/vaccine/by-name/{name}")
    public ResponseEntity<List<VaccineResponseVM>> getByName(
            @PathVariable String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Vaccines by name='{}' pageable={}", name, pageable);
        Page<Vaccine> page = vaccineService.findByName(name, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(VaccineResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/vaccine/by-type/{type}")
    public ResponseEntity<List<VaccineResponseVM>> getByType(
            @PathVariable VaccineType type,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Vaccines by type={} pageable={}", type, pageable);
        Page<Vaccine> page = vaccineService.findByType(type, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(VaccineResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/vaccine/by-roa/{roa}")
    public ResponseEntity<List<VaccineResponseVM>> getByRoa(
            @PathVariable RouteOfAdministration roa,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Vaccines by roa={} pageable={}", roa, pageable);
        Page<Vaccine> page = vaccineService.findByRoa(roa, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(VaccineResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @PatchMapping("/vaccine/{id}/toggle-active")
    public ResponseEntity<VaccineResponseVM> toggleVaccineActiveStatus(@PathVariable Long id) {
        LOG.debug("REST toggle Vaccine isActive id={}", id);
        return vaccineService.toggleIsActive(id)
                .map(VaccineResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
