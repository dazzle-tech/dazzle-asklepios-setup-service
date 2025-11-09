package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsNurseVisbility;
import com.dazzle.asklepios.service.DepartmentMedicalSheetsNurseVisibilityService;
import com.dazzle.asklepios.web.rest.vm.DepartmentMedicalSheetsNurseVisibilityVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/setup/department-medical-sheets-nurse")
public class DepartmentMedicalSheetsNurseVisibilityController {

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentMedicalSheetsNurseVisibilityController.class);
    private final DepartmentMedicalSheetsNurseVisibilityService service;

    public DepartmentMedicalSheetsNurseVisibilityController(DepartmentMedicalSheetsNurseVisibilityService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DepartmentMedicalSheetsNurseVisibilityVM> create(
            @RequestBody DepartmentMedicalSheetsNurseVisibilityVM vm) throws URISyntaxException {

        LOG.info("Request to create DepartmentMedicalSheetsNurseVisibility for departmentId={} and medicalSheet={}",
                vm.departmentId(), vm.medicalSheet());

        DepartmentMedicalSheetsNurseVisbility entity = new DepartmentMedicalSheetsNurseVisbility();
        entity.setDepartmentId(vm.departmentId());
        entity.setMedicalSheet(vm.medicalSheet());

        DepartmentMedicalSheetsNurseVisbility saved = service.create(entity);
        LOG.debug("Created entity with id={}, departmentId={}, medicalSheet={}",
                saved.getId(), saved.getDepartmentId(), saved.getMedicalSheet());

        DepartmentMedicalSheetsNurseVisibilityVM result = DepartmentMedicalSheetsNurseVisibilityVM.ofEntity(saved);
        return ResponseEntity.created(new URI("/api/setup/department-medical-sheets-nurse/" + result.departmentId()))
                .body(result);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentMedicalSheetsNurseVisibilityVM>> getAll() {
        LOG.info("Request to get all DepartmentMedicalSheetsNurseVisibility records");
        List<DepartmentMedicalSheetsNurseVisibilityVM> result = service.findAll().stream()
                .map(DepartmentMedicalSheetsNurseVisibilityVM::ofEntity)
                .toList();
        LOG.debug("Retrieved {} records", result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<DepartmentMedicalSheetsNurseVisibilityVM>> getByDepartment(@PathVariable Long departmentId) {
        LOG.info("Request to get DepartmentMedicalSheetsNurseVisibility records for departmentId={}", departmentId);
        List<DepartmentMedicalSheetsNurseVisibilityVM> result = service.findByDepartmentId(departmentId).stream()
                .map(DepartmentMedicalSheetsNurseVisibilityVM::ofEntity)
                .toList();
        LOG.debug("Found {} records for departmentId={}", result.size(), departmentId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.warn("Request to delete DepartmentMedicalSheetsNurseVisibility with id={}", id);
        service.delete(id);
        LOG.debug("Deleted record with id={}", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<DepartmentMedicalSheetsNurseVisibilityVM>> bulkSave(
            @RequestBody List<DepartmentMedicalSheetsNurseVisibilityVM> list) {

        LOG.info("Request to bulk save {} DepartmentMedicalSheetsNurseVisibility records", list.size());
        LOG.debug("bulk save request payload: {}", list);

        List<DepartmentMedicalSheetsNurseVisbility> entities = list.stream().map(vm -> {
            DepartmentMedicalSheetsNurseVisbility e = new DepartmentMedicalSheetsNurseVisbility();
            e.setDepartmentId(vm.departmentId());
            e.setMedicalSheet(vm.medicalSheet());
            return e;
        }).toList();

        List<DepartmentMedicalSheetsNurseVisibilityVM> result = service.bulkSave(entities).stream()
                .map(DepartmentMedicalSheetsNurseVisibilityVM::ofEntity)
                .toList();

        LOG.debug("Successfully bulk saved {} records", result.size());
        return ResponseEntity.ok(result);
    }
}
