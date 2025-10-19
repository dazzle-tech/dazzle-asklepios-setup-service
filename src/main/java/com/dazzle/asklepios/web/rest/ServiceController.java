package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.service.ServiceService;
import com.dazzle.asklepios.web.rest.vm.ServiceCreateVM;
import com.dazzle.asklepios.web.rest.vm.ServiceResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceUpdateVM;
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

/**
 * REST controller for managing Service entities using VMs (scoped by facility).
 */
@RestController
@RequestMapping("/api/setup/service")
public class ServiceController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceController.class);
    private static final String TOTAL_COUNT = "X-Total-Count";

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    /**
     * POST /api/setup/service?facilityId= : Create a new Service for a facility.
     */
    @PostMapping
    public ResponseEntity<ServiceResponseVM> createService(
            @RequestParam Long facilityId,
            @Valid @RequestBody ServiceCreateVM vm
    ) {
        LOG.debug("REST create Service facilityId={} payload={}", facilityId, vm);

        if (vm.name() != null && serviceService.existsByNameIgnoreCase(facilityId, vm.name())) {
            return ResponseEntity.status(409).build();
        }

        var created = serviceService.create(facilityId, vm);

        // نحافظ على نفس الـ base path ونمرّر الـ facilityId كـ query param في Location
        return ResponseEntity
                .created(URI.create("/api/setup/service/" + created.getId() + "?facilityId=" + facilityId))
                .body(ServiceResponseVM.ofEntity(created));
    }

    /**
     * PUT /api/setup/service/{id}?facilityId= : Update an existing Service for a facility.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponseVM> updateService(
            @PathVariable Long id,
            @RequestParam Long facilityId,
            @Valid @RequestBody ServiceUpdateVM vm
    ) {
        LOG.debug("REST update Service id={} facilityId={} payload={}", id, facilityId, vm);
        return serviceService.update(id, facilityId, vm)
                .map(ServiceResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/setup/service?facilityId=&page=&size=&sort=
     * Get all services for a facility (paginated).
     */
    @GetMapping
    public ResponseEntity<List<ServiceResponseVM>> getAllServices(
            @RequestParam Long facilityId,
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false, defaultValue = "id,asc") String sort
    ) {
        LOG.debug("REST list Services facilityId={} page={} size={} sort={}", facilityId, page, size, sort);
        Pageable pageable = buildPageable(page, size, sort);
        var pageResult = serviceService.findAll(facilityId, pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TOTAL_COUNT);

        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    /**
     * GET /api/setup/service/{id}?facilityId= : Get a single service for a facility.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseVM> getService(
            @PathVariable Long id,
            @RequestParam Long facilityId
    ) {
        LOG.debug("REST get Service id={} facilityId={}", id, facilityId);
        return serviceService.findOne(id, facilityId)
                .map(ServiceResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/setup/service/service-list-by-category?facilityId=&category=&page=&size=&sort=
     */
    @GetMapping("/service-list-by-category")
    public ResponseEntity<List<ServiceResponseVM>> getServicesByCategory(
            @RequestParam Long facilityId,
            @RequestParam ServiceCategory category,
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false, defaultValue = "id,asc") String sort
    ) {
        LOG.debug("REST list Services by category facilityId={} category={} page={} size={} sort={}",
                facilityId, category, page, size, sort);
        Pageable pageable = buildPageable(page, size, sort);
        var pageResult = serviceService.findByCategory(facilityId, category, pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TOTAL_COUNT);

        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    /**
     * GET /api/setup/service/service-list-by-code?facilityId=&code=&page=&size=&sort=
     */
    @GetMapping("/service-list-by-code")
    public ResponseEntity<List<ServiceResponseVM>> getServicesByCode(
            @RequestParam Long facilityId,
            @RequestParam String code,
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false, defaultValue = "id,asc") String sort
    ) {
        LOG.debug("REST list Services by code facilityId={} code='{}' page={} size={} sort={}",
                facilityId, code, page, size, sort);
        Pageable pageable = buildPageable(page, size, sort);
        var pageResult = serviceService.findByCodeContainingIgnoreCase(facilityId, code, pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TOTAL_COUNT);

        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    /**
     * GET /api/setup/service/service-list-by-name?facilityId=&name=&page=&size=&sort=
     */
    @GetMapping("/service-list-by-name")
    public ResponseEntity<List<ServiceResponseVM>> getServicesByName(
            @RequestParam Long facilityId,
            @RequestParam String name,
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false, defaultValue = "id,asc") String sort
    ) {
        LOG.debug("REST list Services by name facilityId={} name='{}' page={} size={} sort={}",
                facilityId, name, page, size, sort);
        Pageable pageable = buildPageable(page, size, sort);
        var pageResult = serviceService.findByNameContainingIgnoreCase(facilityId, name, pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TOTAL_COUNT);

        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    /**
     * PATCH /api/setup/service/{id}/toggle-active?facilityId=
     */
    @PatchMapping("/{id}/toggle-active")
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

    // -------------------------
    // Helpers
    // -------------------------
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
}
