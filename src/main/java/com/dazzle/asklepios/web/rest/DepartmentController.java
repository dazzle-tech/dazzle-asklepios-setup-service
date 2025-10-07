package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.service.DepartmentService;
import com.dazzle.asklepios.web.rest.vm.DepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.DepartmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.DepartmentUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup/department")
public class DepartmentController {

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentController.class);
    private static final String TOTAL_COUNT = "X-Total-Count";

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ResponseEntity<DepartmentResponseVM> createDepartment(@Valid @RequestBody DepartmentCreateVM departmentVM) {
        var result = departmentService.create(departmentVM);
        return ResponseEntity
                .created(URI.create("/setup/api/department/" + result.getId()))
                .body(DepartmentResponseVM.ofEntity(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponseVM> updateDepartment(@PathVariable Long id,@Valid @RequestBody DepartmentUpdateVM departmentUpdateVM) {
        return departmentService.update(id, departmentUpdateVM)
                .map(DepartmentResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponseVM>> getAllDepartments(@RequestParam Integer page,@RequestParam Integer size,@RequestParam(required = false, defaultValue = "id,asc") String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        var pageResult = departmentService.findAll(pageable);
        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TOTAL_COUNT);
        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseVM> getDepartment(@PathVariable Long id) {
        return departmentService.findOne(id)
                .map(DepartmentResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<DepartmentResponseVM>> getDepartmentByFacility(@PathVariable("facilityId") Long facilityId,@RequestParam Integer page,@RequestParam Integer size,@RequestParam(required = false, defaultValue = "id,asc") String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        var pageResult = departmentService.findByFacilityId(facilityId, pageable);
        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TOTAL_COUNT);
        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    @GetMapping("/department-list-by-type")
    public ResponseEntity<List<DepartmentResponseVM>> getDepartmentByType(@RequestParam DepartmentType type, @RequestParam Integer page,@RequestParam Integer size,@RequestParam(required = false, defaultValue = "id,asc") String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        var pageResult = departmentService.findByDepartmentType(type, pageable);
        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TOTAL_COUNT);
        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    @GetMapping("/department-list-by-name")
    public ResponseEntity<List<DepartmentResponseVM>> getDepartmentByName(@RequestParam String name,@RequestParam Integer page,@RequestParam Integer size,@RequestParam(required = false, defaultValue = "id,asc") String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        var pageResult = departmentService.findByDepartmentName(name, pageable);
        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TOTAL_COUNT);
        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<DepartmentResponseVM> toggleDepartmentActiveStatus(@PathVariable Long id) {
        return departmentService.toggleIsActive(id)
                .map(DepartmentResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Pageable buildPageable(Integer page, Integer size, String sort) {
        int p = Math.max(0, page);
        int s = Math.max(1, size);
        String[] parts = sort.split(",", 2);
        String prop = parts[0];
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(p, s, Sort.by(dir, prop));
    }
}
