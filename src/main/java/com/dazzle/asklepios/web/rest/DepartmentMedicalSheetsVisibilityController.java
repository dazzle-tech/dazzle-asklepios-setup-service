package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.DepartmentMedicalSheetsVisibilityService;
import com.dazzle.asklepios.web.rest.vm.DepartmentMedicalSheetsVisibilityVM;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/setup/department-medical-sheets")
public class DepartmentMedicalSheetsVisibilityController {

    private final DepartmentMedicalSheetsVisibilityService service;

    public DepartmentMedicalSheetsVisibilityController(DepartmentMedicalSheetsVisibilityService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DepartmentMedicalSheetsVisibilityVM> create(
            @RequestBody DepartmentMedicalSheetsVisibilityVM vm) throws URISyntaxException {
        DepartmentMedicalSheetsVisibilityVM result = service.create(vm);
        return ResponseEntity.created(new URI("/api/department-medical-sheets-visibility/" + result.departmentId()))
                .body(result);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentMedicalSheetsVisibilityVM>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<DepartmentMedicalSheetsVisibilityVM>> getByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(service.findByDepartmentId(departmentId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
