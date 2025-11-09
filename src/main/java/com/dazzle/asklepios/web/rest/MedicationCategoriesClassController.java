package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.MedicationCategories;
import com.dazzle.asklepios.domain.MedicationCategoriesClass;
import com.dazzle.asklepios.service.MedicationCategoriesClassService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/setup/medication-categories-class")
@RequiredArgsConstructor
public class MedicationCategoriesClassController {
    private static final Logger LOG = LoggerFactory.getLogger(MedicationCategoriesClassController.class);
    private final MedicationCategoriesClassService medicationCategoriesClassService;

    @PostMapping
    public ResponseEntity<MedicationCategoriesClass> create(@Valid @RequestBody MedicationCategoriesClass vm) {
        LOG.debug("REST request to create Medication Category Class: {}", vm);
        MedicationCategoriesClass saved = medicationCategoriesClassService.create(vm);
        return ResponseEntity
                .created(URI.create("/api/setup/medication-categories-class/" + saved.getId()))
                .body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicationCategoriesClass> update(@PathVariable Long id,
                                                       @Valid @RequestBody MedicationCategoriesClass vm) {
        LOG.debug("REST request to update Medication Category Class id={} with: {}", id, vm);
        Optional<MedicationCategoriesClass> updated = medicationCategoriesClassService.update(id, vm);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping
    public ResponseEntity<List<MedicationCategoriesClass>> findAll(
            @RequestParam(required = false) String name
    ) {
        LOG.debug("REST request to get all Medication Categories");
        List<MedicationCategoriesClass> list;
        if (name != null && !name.isBlank()) {
            list = medicationCategoriesClassService.findByNameFilter(name.trim());
        } else {
            list = medicationCategoriesClassService.findAll();
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationCategoriesClass> findOne(@PathVariable Long id) {
        LOG.debug("REST request to get MedicationCategories id={}", id);
        return medicationCategoriesClassService.findOne(id)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-category/{id}")
    public ResponseEntity<List<MedicationCategoriesClass>> findAllByCategory(@PathVariable Long id) {
        LOG.debug("REST request to get MedicationCategoryClasses id={}", id);
        return ResponseEntity.ok(medicationCategoriesClassService.findAllByCategoryId(id
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete MedicationCategoryClass id={}", id);
        boolean removed = medicationCategoriesClassService.delete(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
