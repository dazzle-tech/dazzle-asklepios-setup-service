package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.DepartmentServicesService;
import com.dazzle.asklepios.web.rest.vm.department.DepartmentServicesResponseVM;
import com.dazzle.asklepios.web.rest.vm.departmentServices.DepartmentServicesVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class DepartmentServicesController {

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentServicesController.class);

    private final DepartmentServicesService departmentServicesService;

    public DepartmentServicesController(DepartmentServicesService departmentServicesService) {
        this.departmentServicesService = departmentServicesService;
    }

    /**
     * {@code GET /departmentServices/:departmentId/services} : get all services for a department.
     *
     * @param departmentId the id of the department.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of department services.
     */
    @GetMapping("/departmentServices/{departmentId}/services")
    public ResponseEntity<List<DepartmentServicesResponseVM>> getDepartmentServices(@PathVariable Long departmentId) {
        LOG.debug("REST request to get DepartmentServices for departmentId={}", departmentId);

        List<DepartmentServicesResponseVM> result =
                departmentServicesService.findByDepartment(departmentId)
                        .stream()
                        .map(DepartmentServicesResponseVM::ofEntity)
                        .toList();

        return ResponseEntity.ok(result);
    }


    /**
     * {@code PUT /departmentServices/:departmentId/services} : replace all services for a department.
     * Deletes old rows and re-inserts the checked ones.
     *
     * @param departmentId the id of the department.
     * @param vm           the selected services' payload.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the saved list.
     */
    @PutMapping("/departmentServices/{departmentId}/services")
    public ResponseEntity<List<DepartmentServicesResponseVM>> replaceDepartmentServices(
            @PathVariable Long departmentId,
            @RequestBody DepartmentServicesVM vm
    ) {
        LOG.debug("REST request to replace DepartmentServices for departmentId={} with payload={}", departmentId, vm);

        List<DepartmentServicesResponseVM> result =
                departmentServicesService.replaceServices(departmentId, vm.services())
                        .stream()
                        .map(DepartmentServicesResponseVM::ofEntity)
                        .toList();

        return ResponseEntity.ok(result);
    }


}