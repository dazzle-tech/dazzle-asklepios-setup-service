package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.ServiceItemsType;
import com.dazzle.asklepios.service.ServiceItemsService;
import com.dazzle.asklepios.web.rest.vm.DepartmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceItemsCreateVM;
import com.dazzle.asklepios.web.rest.vm.ServiceItemsResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceItemsUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup/service-items")
public class ServiceItemsController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceItemsController.class);
    private static final String TOTAL_COUNT = "X-Total-Count";

    private final ServiceItemsService serviceItemsService;

    public ServiceItemsController(ServiceItemsService serviceItemsService) {
        this.serviceItemsService = serviceItemsService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<ServiceItemsResponseVM> create(@Valid @RequestBody ServiceItemsCreateVM vm) {
        LOG.debug("REST create ServiceItems payload={}", vm);
        var created = serviceItemsService.create(vm);
        return ResponseEntity
                .created(URI.create("/api/setup/service-items/" + created.getId()))
                .body(ServiceItemsResponseVM.ofEntity(created));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ServiceItemsResponseVM> update(@PathVariable Long id,
                                                         @Valid @RequestBody ServiceItemsUpdateVM vm) {
        LOG.debug("REST update ServiceItems id={} payload={}", id, vm);
        return serviceItemsService.update(id, vm)
                .map(ServiceItemsResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // LIST (paginated)
    @GetMapping
    public ResponseEntity<List<ServiceItemsResponseVM>> list(@RequestParam Integer page,
                                                             @RequestParam Integer size,
                                                             @RequestParam(required = false, defaultValue = "id,asc") String sort) {
        LOG.debug("REST list ServiceItems page={} size={} sort={}", page, size, sort);
        Pageable pageable = buildPageable(page, size, sort);
        var pageResult = serviceItemsService.findAll(pageable);
        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TOTAL_COUNT);
        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    // GET by id
    @GetMapping("/{id}")
    public ResponseEntity<ServiceItemsResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST get ServiceItems id={}", id);
        return serviceItemsService.findOne(id)
                .map(ServiceItemsResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // LIST by serviceId (paginated)
    @GetMapping("/by-service/{serviceId}")
    public ResponseEntity<List<ServiceItemsResponseVM>> listByService(@PathVariable Long serviceId,
                                                                      @RequestParam Integer page,
                                                                      @RequestParam Integer size,
                                                                      @RequestParam(required = false, defaultValue = "id,asc") String sort) {
        LOG.debug("REST list ServiceItems by serviceId={} page={} size={} sort={}", serviceId, page, size, sort);
        Pageable pageable = buildPageable(page, size, sort);
        var pageResult = serviceItemsService.findByServiceId(serviceId, pageable);
        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TOTAL_COUNT);
        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }




    // TOGGLE isActive
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ServiceItemsResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle ServiceItems isActive id={}", id);
        return serviceItemsService.toggleIsActive(id)
                .map(ServiceItemsResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete ServiceItems id={}", id);
        serviceItemsService.delete(id);
        return ResponseEntity.noContent().build();
    }


    private Pageable buildPageable(Integer page, Integer size, String sort) {
        LOG.debug("Build pageable page={} size={} sort={}", page, size, sort);
        int p = Math.max(0, page);
        int s = Math.max(1, size);
        String[] parts = sort.split(",", 2);
        String prop = parts[0];
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(p, s, Sort.by(dir, prop));
    }

    @GetMapping("/sources/by-facility")
    public ResponseEntity<List<DepartmentResponseVM>> listSourcesByTypeAndFacility(
            @RequestParam ServiceItemsType type,
            @RequestParam Long facilityId) {
        LOG.debug("REST list sources (no pagination) by type={} facilityId={}", type, facilityId);
        var result = serviceItemsService.findSourcesByTypeAndFacility(type, facilityId);
        return ResponseEntity.ok(result);
    }

}
