package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.VaccineBrands;
import com.dazzle.asklepios.service.VaccineBrandsService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.vaccineBrands.VaccineBrandCreateVM;
import com.dazzle.asklepios.web.rest.vm.vaccineBrands.VaccineBrandResponseVM;
import com.dazzle.asklepios.web.rest.vm.vaccineBrands.VaccineBrandUpdateVM;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/setup")
public class VaccineBrandsController {

    private static final Logger LOG = LoggerFactory.getLogger(VaccineBrandsController.class);

    private final VaccineBrandsService vaccineBrandsService;

    public VaccineBrandsController(VaccineBrandsService vaccineBrandsService) {
        this.vaccineBrandsService = vaccineBrandsService;
    }

    // ====================== CREATE ======================
    @PostMapping("/vaccine-brands")
    public ResponseEntity<VaccineBrandResponseVM> createVaccineBrand(
            @RequestParam Long vaccineId,
            @Valid @RequestBody VaccineBrandCreateVM vm
    ) {
        LOG.debug("REST create VaccineBrand vaccineId={} payload={}", vaccineId, vm);

        VaccineBrands toCreate = VaccineBrands.builder()
                .name(vm.name())
                .manufacture(vm.manufacture())
                .volume(vm.volume())
                .unit(vm.unit())
                .marketingAuthorizationHolder(vm.marketingAuthorizationHolder())
                .isActive(Boolean.TRUE.equals(vm.isActive()))
                .build();

        VaccineBrands created = vaccineBrandsService.create(vaccineId, toCreate);
        VaccineBrandResponseVM body = VaccineBrandResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/vaccine-brands/" + created.getId() + "?vaccineId=" + vaccineId))
                .body(body);
    }

    // ====================== UPDATE ======================
    @PutMapping("/vaccine-brands/{id}")
    public ResponseEntity<VaccineBrandResponseVM> updateVaccineBrand(
            @PathVariable Long id,
            @RequestParam Long vaccineId,
            @Valid @RequestBody VaccineBrandUpdateVM vm
    ) {
        LOG.debug("REST update VaccineBrand id={} vaccineId={} payload={}", id, vaccineId, vm);

        VaccineBrands patch = new VaccineBrands();
        patch.setName(vm.name());
        patch.setManufacture(vm.manufacture());
        patch.setVolume(vm.volume());
        patch.setUnit(vm.unit());
        patch.setMarketingAuthorizationHolder(vm.marketingAuthorizationHolder());
        patch.setIsActive(vm.isActive());

        return vaccineBrandsService.update(id, vaccineId, patch)
                .map(VaccineBrandResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ====================== READ: BY VACCINE (paged) ======================
    @GetMapping("/vaccine-brands/by-vaccine/{vaccineId}")
    public ResponseEntity<List<VaccineBrandResponseVM>> getByVaccine(
            @PathVariable Long vaccineId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list VaccineBrands by vaccineId={} pageable={}", vaccineId, pageable);
        Page<VaccineBrands> page = vaccineBrandsService.findByVaccineId(vaccineId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(VaccineBrandResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // ====================== TOGGLE ACTIVE ======================
    @PatchMapping("/vaccine-brands/{id}/toggle-active")
    public ResponseEntity<VaccineBrandResponseVM> toggleVaccineBrandActiveStatus(@PathVariable Long id) {
        LOG.debug("REST toggle VaccineBrand isActive id={}", id);
        return vaccineBrandsService.toggleIsActive(id)
                .map(VaccineBrandResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
