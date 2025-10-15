package com.dazzle.asklepios.web.rest;

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
        LOG.info("Request to create DepartmentMedicalSheetsVisibility: {}", vm);
        DepartmentMedicalSheetsVisibilityVM result = service.create(vm);
        LOG.debug("Created DepartmentMedicalSheetsVisibility with departmentId={} and medicalSheet={}",
                result.departmentId(), result.medicalSheet());
        return ResponseEntity.created(new URI("/api/department-medical-sheets-visibility/" + result.departmentId()))
                .body(result);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentMedicalSheetsVisibilityVM>> getAll() {
        LOG.info("Request to get all DepartmentMedicalSheetsVisibility entries");
        List<DepartmentMedicalSheetsVisibilityVM> result = service.findAll();
        LOG.debug("Found {} DepartmentMedicalSheetsVisibility entries", result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<DepartmentMedicalSheetsVisibilityVM>> getByDepartment(@PathVariable Long departmentId) {
        LOG.info("Request to get DepartmentMedicalSheetsVisibility entries for departmentId={}", departmentId);
        List<DepartmentMedicalSheetsVisibilityVM> result = service.findByDepartmentId(departmentId);
        LOG.debug("Found {} entries for departmentId={}", result.size(), departmentId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.warn("Request to delete DepartmentMedicalSheetsVisibility with id={}", id);
        service.delete(id);
        LOG.debug("Deleted DepartmentMedicalSheetsVisibility with id={}", id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/bulk")
    public ResponseEntity<List<DepartmentMedicalSheetsVisibilityVM>> bulkSave(
            @RequestBody List<DepartmentMedicalSheetsVisibilityVM> list
    ) {
        LOG.info("Request to bulk save {} DepartmentMedicalSheetsVisibility entries", list.size());
        if (LOG.isDebugEnabled()) {
            list.forEach(item ->
                    LOG.debug(" -> departmentId={}, medicalSheet={}", item.departmentId(), item.medicalSheet())
            );
        }
        List<DepartmentMedicalSheetsVisibilityVM> result = service.bulkSave(list);
        LOG.debug("Successfully bulk saved {} DepartmentMedicalSheetsVisibility entries", result.size());
        return ResponseEntity.ok(result);
    }
}
