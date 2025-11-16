package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.AgeGroup;
import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.DuplicationCandidate;
import com.dazzle.asklepios.domain.Language;
import com.dazzle.asklepios.domain.MedicationCategories;
import com.dazzle.asklepios.service.MedicationCategoriesService;
import com.dazzle.asklepios.web.rest.vm.DuplicationCandidateResponseVM;
import com.dazzle.asklepios.web.rest.vm.ageGroup.AgeGroupResponseVM;
import com.dazzle.asklepios.web.rest.vm.department.DepartmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.medicationcategories.MedicationCategoriesCreateVM;
import com.dazzle.asklepios.web.rest.vm.medicationcategories.MedicationCategoriesResponseVM;
import com.dazzle.asklepios.web.rest.vm.medicationcategories.MedicationCategoriesUpdateVM;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/setup/medication-categories")
@RequiredArgsConstructor
public class MedicationCategoriesController {

    private static final Logger LOG = LoggerFactory.getLogger(MedicationCategoriesController.class);
    private final MedicationCategoriesService medicationCategoriesService;

    @PostMapping
    public ResponseEntity<MedicationCategoriesResponseVM> create(@Valid @RequestBody MedicationCategoriesCreateVM vm) {
        LOG.debug("REST create Medication Categories ={}", vm);

        MedicationCategories toCreate = MedicationCategories.builder()
                .name(vm.name())
                .build();

        MedicationCategories created = medicationCategoriesService.create(toCreate);
        MedicationCategoriesResponseVM body = MedicationCategoriesResponseVM.mapEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/medication-categories/" + created.getId()))
                .body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicationCategoriesResponseVM> update(@PathVariable Long id,
                                           @Valid @RequestBody MedicationCategoriesUpdateVM vm) {
        LOG.debug("REST request to update Medication Category id={} with: {}", id, vm);
        MedicationCategories toUpdate = MedicationCategories.builder()
                .name(vm.name())
                .id(id)
                .build();

        return medicationCategoriesService.update(id, toUpdate)
                .map(MedicationCategoriesResponseVM::mapEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }


    @GetMapping
    public ResponseEntity<List<MedicationCategories>> findAll(
            @RequestParam(required = false) String name
    ) {
        LOG.debug("REST request to get all Medication Categories");

        List<MedicationCategories> list;
        if (name != null && !name.isBlank()) {
            list = medicationCategoriesService.findByNameFilter(name.trim());
        } else {
            list = medicationCategoriesService.findAll();
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationCategories> findOne(@PathVariable Long id) {
        LOG.debug("REST request to get MedicationCategories id={}", id);
        return medicationCategoriesService.findOne(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete MedicationCategory id={}", id);
        boolean removed = medicationCategoriesService.delete(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
