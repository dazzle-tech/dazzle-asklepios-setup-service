package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.MedicationCategories;
import com.dazzle.asklepios.domain.MedicationCategoriesClass;
import com.dazzle.asklepios.service.MedicationCategoriesClassService;
import com.dazzle.asklepios.web.rest.vm.medicationcategories.MedicationCategoriesResponseVM;
import com.dazzle.asklepios.web.rest.vm.medicationcategoriesclass.MedicationCategoriesClassCreateVM;
import com.dazzle.asklepios.web.rest.vm.medicationcategoriesclass.MedicationCategoriesClassResponseVM;
import com.dazzle.asklepios.web.rest.vm.medicationcategoriesclass.MedicationCategoriesClassUpdateVM;
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
    public ResponseEntity<MedicationCategoriesClassResponseVM> create(@Valid @RequestBody MedicationCategoriesClassCreateVM vm) {
        LOG.debug("REST create Medication Category class ={}", vm);
        MedicationCategoriesClass toCreate = MedicationCategoriesClass.builder()
                .name(vm.name())
                .medicationCategoriesId(vm.medicationCategoriesId())
                .build();

        MedicationCategoriesClass created = medicationCategoriesClassService.create(toCreate);
        MedicationCategoriesClassResponseVM body = MedicationCategoriesClassResponseVM.mapEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/medication-categories-class/" + created.getId()))
                .body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicationCategoriesClassResponseVM> update(@PathVariable Long id,
                                                       @Valid @RequestBody MedicationCategoriesClassUpdateVM vm) {
        LOG.debug("REST request to update Medication Category class id={} with: {}", id, vm);
        MedicationCategoriesClass toUpdate = MedicationCategoriesClass.builder()
                .name(vm.name())
                .id(id)
                .medicationCategoriesId(vm.medicationCategoriesId())
                .build();
        return medicationCategoriesClassService.update(id, toUpdate)
                .map(MedicationCategoriesClassResponseVM::mapEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping
    public ResponseEntity<List<MedicationCategoriesClass>> findAll(
            @RequestParam(required = false) String name
    ) {
        LOG.debug("REST request to get all Medication Categories class");
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
        LOG.debug("REST request to get Medication Category class id={}", id);
        return medicationCategoriesClassService.findOne(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-category/{id}")
    public ResponseEntity<List<MedicationCategoriesClass>> findAllByCategory(@PathVariable Long id) {
        LOG.debug("REST request to get MedicationCategoryClasses id={}", id);
        return ResponseEntity.ok(
                medicationCategoriesClassService.findAllByCategoryId(id).stream()
                        .toList()
        );
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<MedicationCategoriesClass>> findAllByCategoryIdAndName(@RequestParam Long id, @RequestParam(required = false) String name) {
        LOG.debug("REST request to get MedicationCategoryClasses id={}", id);
        return ResponseEntity.ok(medicationCategoriesClassService.findAllByCategoryIdAndName(id, name
        ).stream().toList()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete MedicationCategoryClass id={}", id);
        boolean removed = medicationCategoriesClassService.delete(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
