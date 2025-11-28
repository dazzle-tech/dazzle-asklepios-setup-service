package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.CountryDistrict;
import com.dazzle.asklepios.service.CountryDistrictService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.countryDistrict.CountryDistrictCreateVM;
import com.dazzle.asklepios.web.rest.vm.countryDistrict.CountryDistrictResponseVM;
import com.dazzle.asklepios.web.rest.vm.countryDistrict.CountryDistrictUpdateVM;
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
public class CountryDistrictController {

    private static final Logger LOG = LoggerFactory.getLogger(CountryDistrictController.class);

    private final CountryDistrictService districtService;

    public CountryDistrictController(CountryDistrictService districtService) {
        this.districtService = districtService;
    }

    @PostMapping("/country/{countryId}/district")
    public ResponseEntity<CountryDistrictResponseVM> createDistrict(
            @PathVariable Long countryId,
            @Valid @RequestBody CountryDistrictCreateVM vm
    ) {
        LOG.debug("REST create CountryDistrict countryId={} payload={}", countryId, vm);

        CountryDistrict incoming = CountryDistrict.builder()
                .name(vm.name())
                .code(vm.code())
                .isActive(vm.isActive() != null ? vm.isActive() : Boolean.TRUE)
                .build();

        CountryDistrict created = districtService.create(countryId, incoming);
        CountryDistrictResponseVM body = CountryDistrictResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/country/" + countryId + "/district/" + created.getId()))
                .body(body);
    }

    @PutMapping("/country/{countryId}/district/{id}")
    public ResponseEntity<CountryDistrictResponseVM> updateDistrict(
            @PathVariable Long countryId,
            @PathVariable Long id,
            @Valid @RequestBody CountryDistrictUpdateVM vm
    ) {
        LOG.debug("REST update CountryDistrict id={} payload={}", id, vm);

        CountryDistrict patch = new CountryDistrict();
        patch.setName(vm.name());
        patch.setCode(vm.code());
        patch.setIsActive(vm.isActive());

        return districtService.update(id, patch)
                .map(CountryDistrictResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/district")
    public ResponseEntity<List<CountryDistrictResponseVM>> getAllDistrictsPaged(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list CountryDistricts pageable={}", pageable);

        Page<CountryDistrict> page = districtService.findAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<CountryDistrictResponseVM> body = page.getContent()
                .stream()
                .map(CountryDistrictResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/country/{countryId}/district")
    public ResponseEntity<List<CountryDistrictResponseVM>> getByCountry(
            @PathVariable Long countryId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list districts by countryId={} pageable={}", countryId, pageable);

        Page<CountryDistrict> page = districtService.findByCountry(countryId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<CountryDistrictResponseVM> body = page.getContent()
                .stream()
                .map(CountryDistrictResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
    @GetMapping("/country/{countryId}/district/by-name")
    public ResponseEntity<List<CountryDistrictResponseVM>> getByName(
            @PathVariable Long countryId,
            @RequestParam(required = false) String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list districts by name='{}' for countryId={} pageable={}", name, countryId, pageable);

        Page<CountryDistrict> page = districtService.findByName(countryId, name, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<CountryDistrictResponseVM> body = page.getContent()
                .stream()
                .map(CountryDistrictResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/country/{countryId}/district/by-code")
    public ResponseEntity<List<CountryDistrictResponseVM>> getByCode(
            @PathVariable Long countryId,
            @RequestParam(required = false) String code,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list districts by code='{}' for countryId={} pageable={}", code, countryId, pageable);

        Page<CountryDistrict> page = districtService.findByCode(countryId, code, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<CountryDistrictResponseVM> body = page.getContent()
                .stream()
                .map(CountryDistrictResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @PatchMapping("/district/{id}/toggle-active")
    public ResponseEntity<CountryDistrictResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle CountryDistrict isActive id={}", id);

        return districtService.toggleIsActive(id)
                .map(CountryDistrictResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
