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
 * REST controller for managing Service entities using VMs.
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
     * POST /api/setup/service : Create a new Service.
     */
    @PostMapping
    public ResponseEntity<ServiceResponseVM> createService(@Valid @RequestBody ServiceCreateVM vm) {
        LOG.debug("REST create Service payload={}", vm);

        // Optional: uniqueness check by name (HTTP 409 if already exists)
        if (vm.name() != null && serviceService.existsByNameIgnoreCase(vm.name())) {
            return ResponseEntity.status(409).build();
        }

        var created = serviceService.create(vm);
        return ResponseEntity
                .created(URI.create("/api/setup/service/" + created.getId()))
                .body(ServiceResponseVM.ofEntity(created));
    }

    /**
     * PUT /api/setup/service/{id} : Update an existing Service.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponseVM> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceUpdateVM vm
    ) {
        LOG.debug("REST update Service id={} payload={}", id, vm);
        return serviceService.update(id, vm)
                .map(ServiceResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/setup/service : Get all services (paginated).
     * Adds X-Total-Count header like DepartmentController.
     */
    @GetMapping
    public ResponseEntity<List<ServiceResponseVM>> getAllServices(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false, defaultValue = "id,asc") String sort
    ) {
        LOG.debug("REST list Services page={} size={} sort={}", page, size, sort);
        Pageable pageable = buildPageable(page, size, sort);
        var pageResult = serviceService.findAll(pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TOTAL_COUNT);

        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    /**
     * GET /api/setup/service/{id} : Get a single service.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseVM> getService(@PathVariable Long id) {
        LOG.debug("REST get Service id={}", id);
        return serviceService.findOne(id)
                .map(ServiceResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/setup/service/service-list-by-category : Filter by category (paginated).
     */
    @GetMapping("/service-list-by-category")
    public ResponseEntity<List<ServiceResponseVM>> getServicesByCategory(
            @RequestParam ServiceCategory category,
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false, defaultValue = "id,asc") String sort
    ) {
        LOG.debug("REST list Services by category={} page={} size={} sort={}", category, page, size, sort);
        Pageable pageable = buildPageable(page, size, sort);
        var pageResult = serviceService.findByCategory(category, pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TOTAL_COUNT);

        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    /**
     * GET /api/setup/service/service-list-by-code : Filter by code (paginated).
     */
    @GetMapping("/service-list-by-code")
    public ResponseEntity<List<ServiceResponseVM>> getServicesByCode(
            @RequestParam String code,
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false, defaultValue = "id,asc") String sort
    ) {
        LOG.debug("REST list Services by code='{}' page={} size={} sort={}", code, page, size, sort);
        Pageable pageable = buildPageable(page, size, sort);
        var pageResult = serviceService.findByCodeContainingIgnoreCase(code, pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TOTAL_COUNT);

        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    /**
     * GET /api/setup/service/service-list-by-name : Filter by name contains (paginated).
     */
    @GetMapping("/service-list-by-name")
    public ResponseEntity<List<ServiceResponseVM>> getServicesByName(
            @RequestParam String name,
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false, defaultValue = "id,asc") String sort
    ) {
        LOG.debug("REST list Services by name='{}' page={} size={} sort={}", name, page, size, sort);
        Pageable pageable = buildPageable(page, size, sort);
        var pageResult = serviceService.findByNameContainingIgnoreCase(name, pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TOTAL_COUNT);

        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    /**
     * PATCH /api/setup/service/{id}/toggle-active : Toggle isActive for Service.
     * (Replaces delete)
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ServiceResponseVM> toggleServiceActiveStatus(@PathVariable Long id) {
        LOG.debug("REST toggle Service isActive id={}", id);
        return serviceService.toggleIsActive(id)
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
        // Ensure imports:
        // import org.springframework.data.domain.PageRequest;
        // import org.springframework.data.domain.Pageable;
        // import org.springframework.data.domain.Sort;
    }
}
