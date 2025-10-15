package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.DepartmentMedicalSheetsNurseVisibilityService;
import com.dazzle.asklepios.web.rest.vm.DepartmentMedicalSheetsNurseVisibilityVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        LOG.info("Request to create DepartmentMedicalSheetsNurseVisibility: {}", vm);
        DepartmentMedicalSheetsNurseVisibilityVM result = service.create(vm);
        LOG.debug("Created DepartmentMedicalSheetsNurseVisibility with departmentId={} and medicalSheet={}",
                result.departmentId(), result.medicalSheet());
        return ResponseEntity.created(new URI("/api/setup/department-medical-sheets-nurse/" + result.departmentId()))
                .body(result);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentMedicalSheetsNurseVisibilityVM>> getAll() {
        LOG.info("Request to get all DepartmentMedicalSheetsNurseVisibility entries");
        List<DepartmentMedicalSheetsNurseVisibilityVM> result = service.findAll();
        LOG.debug("Found {} DepartmentMedicalSheetsNurseVisibility entries", result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<DepartmentMedicalSheetsNurseVisibilityVM>> getByDepartment(@PathVariable Long departmentId) {
        LOG.info("Request to get DepartmentMedicalSheetsNurseVisibility entries for departmentId={}", departmentId);
        List<DepartmentMedicalSheetsNurseVisibilityVM> result = service.findByDepartmentId(departmentId);
        LOG.debug("Found {} entries for departmentId={}", result.size(), departmentId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.warn("Request to delete DepartmentMedicalSheetsNurseVisibility with id={}", id);
        service.delete(id);
        LOG.debug("Deleted DepartmentMedicalSheetsNurseVisibility with id={}", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<DepartmentMedicalSheetsNurseVisibilityVM>> bulkSave(
            @RequestBody List<DepartmentMedicalSheetsNurseVisibilityVM> list
    ) {
        LOG.info("Request to bulk save {} DepartmentMedicalSheetsNurseVisibility entries", list.size());
        if (LOG.isDebugEnabled()) {
            list.forEach(item ->
                    LOG.debug(" -> departmentId={}, medicalSheet={}", item.departmentId(), item.medicalSheet())
            );
        }
        List<DepartmentMedicalSheetsNurseVisibilityVM> result = service.bulkSave(list);
        LOG.debug("Successfully bulk saved {} DepartmentMedicalSheetsNurseVisibility entries", result.size());
        return ResponseEntity.ok(result);
    }
}
