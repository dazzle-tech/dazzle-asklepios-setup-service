package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.ServiceItems;
import com.dazzle.asklepios.domain.enumeration.ServiceItemsType;
import com.dazzle.asklepios.service.ServiceItemsService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.DepartmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceItemsCreateVM;
import com.dazzle.asklepios.web.rest.vm.ServiceItemsResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceItemsUpdateVM;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class ServiceItemsController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceItemsController.class);

    private final ServiceItemsService serviceItemsService;

    public ServiceItemsController(ServiceItemsService serviceItemsService) {
        this.serviceItemsService = serviceItemsService;
    }


    @PostMapping("/service-items")
    public ResponseEntity<ServiceItemsResponseVM> create(@Valid @RequestBody ServiceItemsCreateVM vm) {
        LOG.debug("REST create ServiceItems payload={}", vm);

        ServiceItems toCreate = ServiceItems.builder()
                .type(vm.type())
                .sourceId(vm.sourceId())
                .isActive(vm.isActive() != null ? vm.isActive() : Boolean.TRUE)
                .build();

        ServiceItems created = serviceItemsService.create(vm.serviceId(), toCreate);
        return ResponseEntity
                .created(URI.create("/setup/api/service-items/" + created.getId()))
                .body(ServiceItemsResponseVM.ofEntity(created));
    }


    @PutMapping("/service-items/{id}")
    public ResponseEntity<ServiceItemsResponseVM> update(@PathVariable Long id,
                                                         @Valid @RequestBody ServiceItemsUpdateVM vm) {
        LOG.debug("REST update ServiceItems id={} payload={}", id, vm);

        ServiceItems patch = ServiceItems.builder()
                .type(vm.type())
                .sourceId(vm.sourceId())
                .isActive(vm.isActive())
                .build();
        patch.setLastModifiedBy(vm.lastModifiedBy());

        return serviceItemsService.update(id, vm.serviceId(), patch)
                .map(ServiceItemsResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/service-items")
    public ResponseEntity<List<ServiceItemsResponseVM>> list(@ParameterObject Pageable pageable) {
        LOG.debug("REST list ServiceItems pageable={}", pageable);
        final Page<ServiceItems> page = serviceItemsService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(ServiceItemsResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }


    @GetMapping("/service-items/{id}")
    public ResponseEntity<ServiceItemsResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST get ServiceItems id={}", id);
        return serviceItemsService.findOne(id)
                .map(ServiceItemsResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/service-items/by-service/{serviceId}")
    public ResponseEntity<List<ServiceItemsResponseVM>> listByService(@PathVariable Long serviceId,
                                                                      @ParameterObject Pageable pageable) {
        LOG.debug("REST list ServiceItems by serviceId={} pageable={}", serviceId, pageable);
        final Page<ServiceItems> page = serviceItemsService.findByServiceId(serviceId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(ServiceItemsResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }


    @PatchMapping("/service-items/{id}/toggle-active")
    public ResponseEntity<ServiceItemsResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle ServiceItems isActive id={}", id);
        return serviceItemsService.toggleIsActive(id)
                .map(ServiceItemsResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @DeleteMapping("/service-items/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete ServiceItems id={}", id);
        serviceItemsService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/service-items/sources/by-facility")
    public ResponseEntity<List<DepartmentResponseVM>> listSourcesByTypeAndFacility(
            @RequestParam ServiceItemsType type,
            @RequestParam Long facilityId) {

        LOG.debug("REST list sources (no pagination) by type={} facilityId={}", type, facilityId);

        List<Department> departments = serviceItemsService.findSourcesByTypeAndFacility(type, facilityId);

        // Convert domain entities to response VMs
        List<DepartmentResponseVM> response = departments.stream()
                .map(DepartmentResponseVM::ofEntity)
                .toList();

        // Return 200 OK with data (empty list for unsupported types)
        return ResponseEntity.ok(response);
    }

}
