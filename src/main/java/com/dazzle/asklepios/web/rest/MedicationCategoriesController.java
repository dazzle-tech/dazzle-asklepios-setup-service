package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Language;
import com.dazzle.asklepios.domain.MedicationCategories;
import com.dazzle.asklepios.service.MedicationCategoriesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/setup/medication-categories")
@RequiredArgsConstructor
public class MedicationCategoriesController {

    private static final Logger LOG = LoggerFactory.getLogger(MedicationCategoriesController.class);
    private final MedicationCategoriesService medicationCategoriesService;

    @PostMapping
    public ResponseEntity<MedicationCategories> create(@Valid @RequestBody MedicationCategories vm) {
        LOG.debug("REST request to create Medication Category: {}", vm);
        MedicationCategories saved = medicationCategoriesService.create(vm);
        return ResponseEntity
                .created(URI.create("/api/setup/medication-categories/" + saved.getId()))
                .body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicationCategories> update(@PathVariable Long id,
                                           @Valid @RequestBody MedicationCategories vm) {
        LOG.debug("REST request to update Medication Category id={} with: {}", id, vm);
        Optional<MedicationCategories> updated = medicationCategoriesService.update(id, vm);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping
    public ResponseEntity<List<MedicationCategories>> findAll() {
        LOG.debug("REST request to get all Medication Categories");
        return ResponseEntity.ok(medicationCategoriesService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationCategories> findOne(@PathVariable Long id) {
        LOG.debug("REST request to get MedicationCategories id={}", id);
        return medicationCategoriesService.findOne(id)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete MedicationCategory id={}", id);
        boolean removed = medicationCategoriesService.delete(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
