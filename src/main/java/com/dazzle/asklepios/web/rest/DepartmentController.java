package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.service.DepartmentService;
import com.dazzle.asklepios.web.rest.vm.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/setup/department")
public class DepartmentController {

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentController.class);

    private final DepartmentService departmentService;


    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ResponseEntity<DepartmentResponseVM> createDepartment(@Valid @RequestBody DepartmentCreateVM departmentVM) {
        LOG.debug("REST request to save Department : {}", departmentVM);
        Department result = departmentService.create(departmentVM);
        return ResponseEntity
                .created(URI.create("/setup/api/department/" + result.getId()))
                .body(DepartmentResponseVM.ofEntity(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponseVM> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateVM departmentUpdateVM) {

        LOG.debug("REST request to update Department : {}, {}", id, departmentUpdateVM);

        return departmentService.update(id, departmentUpdateVM)
                .map(DepartmentResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponseVM>> getAllDepartments() {
        LOG.debug("REST request to get all Departments");
        List<DepartmentResponseVM> departments = departmentService.findAll();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseVM> getDepartment(@PathVariable Long id) {
        return departmentService.findOne(id)
                .map(DepartmentResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<DepartmentResponseVM>> getDepartmentByFacility(@PathVariable("facilityId") Long facilityId) {
        LOG.debug("REST request to get Departments by Facility facility_id={}", facilityId);
        List<DepartmentResponseVM> departments = departmentService.findByFacilityId(facilityId).stream()
                .map(DepartmentResponseVM::ofEntity)
                .toList();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/department-list-by-type")
    public ResponseEntity<List<DepartmentResponseVM>> getDepartmentByType(@RequestHeader DepartmentType type) {
        LOG.debug("REST request to get Departments by Type department_type={}", type);
        List<DepartmentResponseVM> departments = departmentService.findByDepartmentType(type).stream()
                .map(DepartmentResponseVM::ofEntity)
                .toList();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/department-list-by-name")
    public ResponseEntity<List<DepartmentResponseVM>> getDepartmentByName(@RequestHeader String name) {
        LOG.debug("REST request to get Departments by Name name={}", name);
        List<DepartmentResponseVM> departments = departmentService.findByDepartmentName(name).stream()
                .map(DepartmentResponseVM::ofEntity)
                .toList();
        return ResponseEntity.ok(departments);
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<DepartmentResponseVM> toggleDepartmentActiveStatus(@PathVariable Long id) {
        LOG.debug("REST request to toggle active status of Department with id={}", id);

        return departmentService.toggleIsActive(id)
                .map(DepartmentResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/department-type")
    public ResponseEntity<List<DepartmentType>> getAllDepartmentTypes() {
        List<DepartmentType> departmentTypes = Arrays.asList(DepartmentType.values());
        return ResponseEntity.ok(departmentTypes);
    }

    @GetMapping("/encounter-type")
    public ResponseEntity<List<EncounterType>> getAllEncounterTypes() {
        List<EncounterType> encounterTypes = Arrays.asList(EncounterType.values());
        return ResponseEntity.ok(encounterTypes);
    }
}
