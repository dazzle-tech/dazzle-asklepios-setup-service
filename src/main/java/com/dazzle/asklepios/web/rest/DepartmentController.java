// src/main/java/com/dazzle/asklepios/web/rest/DepartmentController.java
package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.service.DepartmentService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.department.DepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.department.DepartmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.department.DepartmentUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class DepartmentController {

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentController.class);

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * {@code POST /department} : Create a new Department.
     */
    @PostMapping("/department")
    public ResponseEntity<DepartmentResponseVM> createDepartment(@Valid @RequestBody DepartmentCreateVM departmentVM) {
        LOG.debug("REST create Department payload={}", departmentVM);
        Department department = departmentService.create(departmentVM);
        DepartmentResponseVM departmentResponseVM = DepartmentResponseVM.ofEntity(department);
        LOG.debug("REST create Department Response={}", departmentResponseVM);

        return ResponseEntity
                .created(URI.create("/setup/api/department/" + department.getId()))
                .body(departmentResponseVM);
    }

    /**
     * {@code PUT /department/{id}} : Update an existing Department.
     */
    @PutMapping("/department/{id}")
    public ResponseEntity<DepartmentResponseVM> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateVM departmentUpdateVM
    ) {
        LOG.debug("REST update Department id={} payload={}", id, departmentUpdateVM);
        return departmentService.update(id, departmentUpdateVM)
                .map(dept -> ResponseEntity.ok(DepartmentResponseVM.ofEntity(dept)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /department} : Get a paginated list of all Departments.
     */
    @GetMapping("/department")
    public ResponseEntity<List<DepartmentResponseVM>> getAllDepartments(@ParameterObject Pageable pageable) {
        LOG.debug("REST list Departments page={}", pageable);
        final Page<Department> page = departmentService.findAll(pageable);
        LOG.debug("REST list Departments page={}", page.getContent());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(DepartmentResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    /**
     * {@code GET /department/by-facility/{facilityId}} : Get departments for a facility (paginated).
     */
    @GetMapping("/department/by-facility/{facilityId:\\d+}")
    public ResponseEntity<List<DepartmentResponseVM>> getByFacility(
            @PathVariable Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Departments by facilityId={} page={}", facilityId, pageable);
        Page<Department> page = departmentService.findByFacilityId(facilityId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(DepartmentResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/department/by-type/{type}")
    public ResponseEntity<List<DepartmentResponseVM>> getByType(
            @PathVariable DepartmentType type,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Departments by type={} page={}", type, pageable);
        Page<Department> page = departmentService.findByDepartmentType(type, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(DepartmentResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/department/by-type-and-facility/{type}/{facilityId:\\d+}")
    public ResponseEntity<List<DepartmentResponseVM>> getByTypeAndFacility(
            @PathVariable DepartmentType type,
            @PathVariable Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Departments by type={} and facilityId={} page={}", type, facilityId, pageable);

        Page<Department> page = departmentService.findByTypeAndFacilityId(type, facilityId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                page.getContent().stream()
                        .map(DepartmentResponseVM::ofEntity)
                        .toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/department/by-name/{name}")
    public ResponseEntity<List<DepartmentResponseVM>> searchByName(
            @PathVariable String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Departments by name='{}' page={}", name, pageable);
        Page<Department> page = departmentService.findByDepartmentName(name, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(DepartmentResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    /**
     * {@code GET /department/{id}} : Get a single Department by id.
     */
    @GetMapping("/department/{id}")
    public ResponseEntity<DepartmentResponseVM> getDepartment(@PathVariable Long id) {
        LOG.debug("REST get Department id={}", id);
        return departmentService.findOne(id)
                .map(DepartmentResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /department/all} : Get All Departments without pagination.
     */
    @GetMapping("/department/all")
    public ResponseEntity<List<DepartmentResponseVM>> getAllDepartment() {
        LOG.debug("REST get All Department");

        List<DepartmentResponseVM> departments = departmentService.findAll().stream()
                .map(DepartmentResponseVM::ofEntity)
                .toList();

        return ResponseEntity.ok(departments);
    }

    /**
     * {@code PATCH /department/{id}/toggle-active} : Toggle the {@code isActive} status of a Department.
     */
    @PatchMapping("/department/{id}/toggle-active")
    public ResponseEntity<DepartmentResponseVM> toggleDepartmentActiveStatus(@PathVariable Long id) {
        LOG.debug("REST toggle Department isActive id={}", id);
        return departmentService.toggleIsActive(id)
                .map(DepartmentResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/department/facility/{facilityId}/active/list")
    public ResponseEntity<List<Department>> getActiveDepartmentsByFacilityIdList(
            @PathVariable Long facilityId
    ) {
        return ResponseEntity.ok(departmentService.findActiveByFacilityId(facilityId));
    }

    @GetMapping("/department/appointable/by-type/{type}/{facilityId}")
    public ResponseEntity<List<DepartmentResponseVM>> getAppointableByTypeByFacility(
            @PathVariable DepartmentType type,
            @PathVariable Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list appointable Departments by type={} page={}", type, pageable);

        Page<Department> page = departmentService.findAppointableByDepartmentType(type, facilityId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                page.getContent().stream()
                        .map(DepartmentResponseVM::ofEntity)
                        .toList(),
                headers,
                HttpStatus.OK
        );
    }
    @GetMapping("/department/appointable/active/by-type/{type}")
    public ResponseEntity<List<DepartmentResponseVM>> getAppointableByType(
            @PathVariable DepartmentType type,

            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list appointable Departments by type={} page={}", type, pageable);

        Page<Department> page = departmentService.findActiveAppointableByDepartmentType(type, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                page.getContent().stream()
                        .map(DepartmentResponseVM::ofEntity)
                        .toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/department/appointable/{facilityId}")
    public ResponseEntity<List<DepartmentResponseVM>> getAppointableDepartment(
            @PathVariable Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list appointable Departments page={}", pageable);

        Page<Department> page = departmentService.findAppointableDepartment(facilityId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                page.getContent().stream()
                        .map(DepartmentResponseVM::ofEntity)
                        .toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/department/by-resource-type/{resourceType}")
    public ResponseEntity<List<DepartmentResponseVM>> getDepartmentsByResourceType(
            @PathVariable String resourceType
    ) {
        LOG.debug("REST list Departments by resourceType={}", resourceType);

        List<DepartmentResponseVM> departments = departmentService
                .findDepartmentsLinkedToResourceType(resourceType)
                .stream()
                .map(DepartmentResponseVM::ofEntity)
                .toList();

        return ResponseEntity.ok(departments);
    }

}
