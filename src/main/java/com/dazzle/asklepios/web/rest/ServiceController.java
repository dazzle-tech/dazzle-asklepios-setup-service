package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.service.ServiceService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.ServiceCreateVM;
import com.dazzle.asklepios.web.rest.vm.ServiceResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceUpdateVM;
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
public class ServiceController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceController.class);
    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    // ====================== CREATE ======================
    @PostMapping("/service")
    public ResponseEntity<ServiceResponseVM> createService(
            @RequestParam Long facilityId,
            @Valid @RequestBody ServiceCreateVM vm
    ) {
        LOG.debug("REST create Service facilityId={} payload={}", facilityId, vm);

        ServiceSetup toCreate = ServiceSetup.builder()
                .name(vm.name())
                .abbreviation(vm.abbreviation())
                .code(vm.code())
                .category(vm.category())
                .price(vm.price())
                .currency(vm.currency())
                .isActive(Boolean.TRUE.equals(vm.isActive()))
                .build();

        ServiceSetup created = serviceService.create(facilityId, toCreate);
        ServiceResponseVM body = ServiceResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/setup/api/service/" + created.getId() + "?facilityId=" + facilityId))
                .body(body);
    }

    // ====================== UPDATE ======================
    @PutMapping("/service/{id}")
    public ResponseEntity<ServiceResponseVM> updateService(
            @PathVariable Long id,
            @RequestParam Long facilityId,
            @Valid @RequestBody ServiceUpdateVM vm
    ) {
        LOG.debug("REST update Service id={} facilityId={} payload={}", id, facilityId, vm);

        ServiceSetup patch = new ServiceSetup();
        patch.setName(vm.name());
        patch.setAbbreviation(vm.abbreviation());
        patch.setCode(vm.code());
        patch.setCategory(vm.category());
        patch.setPrice(vm.price());
        patch.setCurrency(vm.currency());
        patch.setIsActive(vm.isActive());
        patch.setLastModifiedBy(vm.lastModifiedBy());

        return serviceService.update(id, facilityId, patch)
                .map(ServiceResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    // ====================== READ ALL ======================
    @GetMapping("/service")
    public ResponseEntity<List<ServiceResponseVM>> getAllServices(
            @RequestParam Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Services facilityId={} pageable={}", facilityId, pageable);
        final Page<ServiceSetup> page = serviceService.findAll(facilityId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(ServiceResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // ====================== FILTERS ======================
    @GetMapping("/service/by-category/{category}")
    public ResponseEntity<List<ServiceResponseVM>> getByCategory(
            @PathVariable ServiceCategory category,
            @RequestParam Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Services by category={} facilityId={} pageable={}", category, facilityId, pageable);
        Page<ServiceSetup> page = serviceService.findByCategory(facilityId, category, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(ServiceResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/service/by-code/{code}")
    public ResponseEntity<List<ServiceResponseVM>> getByCode(
            @PathVariable String code,
            @RequestParam Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Services by code='{}' facilityId={} pageable={}", code, facilityId, pageable);
        Page<ServiceSetup> page = serviceService.findByCodeContainingIgnoreCase(facilityId, code, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(ServiceResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/service/by-name/{name}")
    public ResponseEntity<List<ServiceResponseVM>> getByName(
            @PathVariable String name,
            @RequestParam Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Services by name='{}' facilityId={} pageable={}", name, facilityId, pageable);
        Page<ServiceSetup> page = serviceService.findByNameContainingIgnoreCase(facilityId, name, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(ServiceResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // ====================== TOGGLE ACTIVE ======================
    @PatchMapping("/service/{id}/toggle-active")
    public ResponseEntity<ServiceResponseVM> toggleServiceActiveStatus(
            @PathVariable Long id,
            @RequestParam Long facilityId
    ) {
        LOG.debug("REST toggle Service isActive id={} facilityId={}", id, facilityId);
        return serviceService.toggleIsActive(id, facilityId)
                .map(ServiceResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
