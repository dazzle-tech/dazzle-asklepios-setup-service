package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.UomGroupUnit;
import com.dazzle.asklepios.service.UomGroupUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/setup/api/uom-group-unit")
public class UomGroupUnitController {

    private static final Logger LOG = LoggerFactory.getLogger(UomGroupUnitController.class);
    private final UomGroupUnitService service;

    public UomGroupUnitController(UomGroupUnitService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UomGroupUnit> create(@RequestBody UomGroupUnit unit) {
        LOG.debug("REST request to save UOM Group Unit : {}", unit);
        return ResponseEntity.ok(service.create(unit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UomGroupUnit> update(@PathVariable Long id, @RequestBody UomGroupUnit unit) {
        LOG.debug("REST request to update UOM Group Unit : {}", id);
        return service.update(id, unit)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UomGroupUnit>> getAll() {
        LOG.debug("REST request to get all UOM Group Units");
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UomGroupUnit> getOne(@PathVariable Long id) {
        LOG.debug("REST request to get UOM Group Unit : {}", id);
        return service.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete UOM Group Unit : {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
