package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.UomGroupRelation;
import com.dazzle.asklepios.service.UomGroupRelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/setup/api/uom-group-relation")
public class UomGroupRelationController {

    private static final Logger LOG = LoggerFactory.getLogger(UomGroupRelationController.class);
    private final UomGroupRelationService service;

    public UomGroupRelationController(UomGroupRelationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UomGroupRelation> create(@RequestBody UomGroupRelation relation) {
        LOG.debug("REST request to save UOM Group Relation : {}", relation);
        return ResponseEntity.ok(service.create(relation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UomGroupRelation> update(@PathVariable Long id, @RequestBody UomGroupRelation relation) {
        LOG.debug("REST request to update UOM Group Relation : {}", id);
        return service.update(id, relation)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UomGroupRelation>> getAll() {
        LOG.debug("REST request to get all UOM Group Relations");
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UomGroupRelation> getOne(@PathVariable Long id) {
        LOG.debug("REST request to get UOM Group Relation : {}", id);
        return service.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete UOM Group Relation : {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
