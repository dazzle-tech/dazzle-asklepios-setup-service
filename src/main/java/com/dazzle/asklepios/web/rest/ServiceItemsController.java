package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ServiceItems;
import com.dazzle.asklepios.repository.ServiceItemsRepository;
import com.dazzle.asklepios.service.ServiceItemsService;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing ServiceItems entities.
 */
@RestController
@RequestMapping("/setup/api/service-items")
public class ServiceItemsController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceItemsController.class);

    private final ServiceItemsService serviceItemsService;
    private final ServiceItemsRepository serviceItemsRepository;

    public ServiceItemsController(ServiceItemsService serviceItemsService, ServiceItemsRepository serviceItemsRepository) {
        this.serviceItemsService = serviceItemsService;
        this.serviceItemsRepository = serviceItemsRepository;
    }

    /**
     * {@code POST /setup/api/service-items} : Create a new ServiceItems.
     */
    @PostMapping
    public ResponseEntity<ServiceItems> createServiceItems(@Valid @RequestBody ServiceItems serviceItems) {
        LOG.debug("REST request to save ServiceItems : {}", serviceItems);
        ServiceItems result = serviceItemsService.create(serviceItems);
        return ResponseEntity
                .created(URI.create("/setup/api/service-items/" + result.getId()))
                .body(result);
    }

    /**
     * {@code PUT /setup/api/service-items/{id}} : Update an existing ServiceItems.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceItems> updateServiceItems(@PathVariable("id") Long id, @Valid @RequestBody ServiceItems serviceItems) {
        LOG.debug("REST request to update ServiceItems : {}, {}", id, serviceItems);
        Optional<ServiceItems> updated = serviceItemsService.update(id, serviceItems);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /setup/api/service-items} : Get all ServiceItems.
     */
    @GetMapping
    public ResponseEntity<List<ServiceItems>> getAllServiceItems() {
        LOG.debug("REST request to get all ServiceItems");
        List<ServiceItems> items = serviceItemsService.findAll();
        return ResponseEntity.ok(items);
    }

    /**
     * {@code GET /setup/api/service-items/{id}} : Get a ServiceItems by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceItems> getServiceItems(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ServiceItems : {}", id);
        return serviceItemsService.findOne(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code DELETE /setup/api/service-items/{id}} : Delete a ServiceItems by id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceItems(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ServiceItems : {}", id);
        serviceItemsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET /setup/api/service-items/by-service/{serviceId}} : Get all ServiceItems by Service id.
     */
    @GetMapping("/by-service/{serviceId}")
    public ResponseEntity<List<ServiceItems>> getServiceItemsByServiceId(@PathVariable("serviceId") Long serviceId) {
        LOG.debug("REST request to get ServiceItems by Service id : {}", serviceId);
        List<ServiceItems> items = serviceItemsRepository.findByServiceId(serviceId);
        return ResponseEntity.ok(items);
    }
}
