package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.UomGroup;
import com.dazzle.asklepios.repository.UomGroupRepository;
import com.dazzle.asklepios.service.UomGroupService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/setup/api/uomGroup")
public class UomGroupController {

    private static final Logger LOG = LoggerFactory.getLogger(UomGroupController.class);

    private final UomGroupService uomGroupService;
    private final UomGroupRepository uomGroupRepository;

    public UomGroupController(UomGroupService uomGroupService, UomGroupRepository uomGroupRepository) {
        this.uomGroupService = uomGroupService;
        this.uomGroupRepository = uomGroupRepository;
    }

    @PostMapping
    public ResponseEntity<UomGroup> createUomGroup(@Valid @RequestBody UomGroup uomGroup) {
        LOG.debug("REST request to create UOM Group : {}", uomGroup);
        if (uomGroup.getName() != null && uomGroupRepository.existsByNameIgnoreCase(uomGroup.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok(uomGroupService.create(uomGroup));

          }

    @PutMapping("/{id}")
    public ResponseEntity<UomGroup> updateUomGroup(@PathVariable("id") Long id, @Valid @RequestBody UomGroup group) {
        LOG.debug("REST request to update UOM Group : {}, {}", id, group);
        Optional<UomGroup> updated = uomGroupService.update(id, group);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UomGroup>> getAllUomGroups() {
        LOG.debug("REST request to get all UOM Groups");
        return ResponseEntity.ok(uomGroupService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UomGroup> getUomGroup(@PathVariable("id") Long id) {
        LOG.debug("REST request to get UOM Group : {}", id);
        return uomGroupService.findOne(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUomGroup(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete UOM Group : {}", id);
        uomGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
