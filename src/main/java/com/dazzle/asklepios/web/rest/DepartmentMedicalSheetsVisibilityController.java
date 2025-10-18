package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsVisibility;
import com.dazzle.asklepios.service.DepartmentMedicalSheetsVisibilityService;
import com.dazzle.asklepios.web.rest.vm.DepartmentMedicalSheetsVisibilityVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/setup/department-medical-sheets")
public class DepartmentMedicalSheetsVisibilityController {

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentMedicalSheetsVisibilityController.class);
    private final DepartmentMedicalSheetsVisibilityService service;

    public DepartmentMedicalSheetsVisibilityController(DepartmentMedicalSheetsVisibilityService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DepartmentMedicalSheetsVisibilityVM> create(
            @RequestBody DepartmentMedicalSheetsVisibilityVM vm) throws URISyntaxException {

        LOG.info("Request to create DepartmentMedicalSheetsVisibility for departmentId={} and medicalSheet={}",
                vm.departmentId(), vm.medicalSheet());

        DepartmentMedicalSheetsVisibility entity = new DepartmentMedicalSheetsVisibility();
        entity.setDepartmentId(vm.departmentId());
        entity.setMedicalSheet(vm.medicalSheet());

        DepartmentMedicalSheetsVisibility saved = service.create(entity);
        LOG.debug("Created entity with id={}, departmentId={}, medicalSheet={}",
                saved.getId(), saved.getDepartmentId(), saved.getMedicalSheet());

        DepartmentMedicalSheetsVisibilityVM result = DepartmentMedicalSheetsVisibilityVM.ofEntity(saved);

        return ResponseEntity.created(new URI("/api/setup/department-medical-sheets/" + result.departmentId()))
                .body(result);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentMedicalSheetsVisibilityVM>> getAll() {
        LOG.info("Request to get all DepartmentMedicalSheetsVisibility records");
        List<DepartmentMedicalSheetsVisibilityVM> result = service.findAll().stream()
                .map(DepartmentMedicalSheetsVisibilityVM::ofEntity)
                .toList();
        LOG.debug("Retrieved {} records", result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<DepartmentMedicalSheetsVisibilityVM>> getByDepartment(@PathVariable Long departmentId) {
        LOG.info("Request to get DepartmentMedicalSheetsVisibility records for departmentId={}", departmentId);
        List<DepartmentMedicalSheetsVisibilityVM> result = service.findByDepartmentId(departmentId).stream()
                .map(DepartmentMedicalSheetsVisibilityVM::ofEntity)
                .toList();
        LOG.debug("Found {} records for departmentId={}", result.size(), departmentId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.warn("Request to delete DepartmentMedicalSheetsVisibility with id={}", id);
        service.delete(id);
        LOG.debug("Deleted record with id={}", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<DepartmentMedicalSheetsVisibilityVM>> bulkSave(
            @RequestBody List<DepartmentMedicalSheetsVisibilityVM> list) {

        LOG.info("Request to bulk save {} DepartmentMedicalSheetsVisibility records", list.size());
        LOG.debug("bulk save request payload: {}", list);


        List<DepartmentMedicalSheetsVisibility> entities = list.stream().map(vm -> {
            DepartmentMedicalSheetsVisibility e = new DepartmentMedicalSheetsVisibility();
            e.setDepartmentId(vm.departmentId());
            e.setMedicalSheet(vm.medicalSheet());
            return e;
        }).toList();

        List<DepartmentMedicalSheetsVisibilityVM> result = service.bulkSave(entities).stream()
                .map(DepartmentMedicalSheetsVisibilityVM::ofEntity)
                .toList();

        LOG.debug("Successfully bulk saved {} records", result.size());
        return ResponseEntity.ok(result);
    }
}
