package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.UserFacilityDepartmentService;
import com.dazzle.asklepios.web.rest.vm.UserFacilityDepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.UserFacilityDepartmentResponseVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/user-facility-departments")
public class UserFacilityDepartmentController {

    private final Logger log = LoggerFactory.getLogger(UserFacilityDepartmentController.class);
    private final UserFacilityDepartmentService userFacilityDepartmentService;

    public UserFacilityDepartmentController(UserFacilityDepartmentService userFacilityDepartmentService) {
        this.userFacilityDepartmentService = userFacilityDepartmentService;
    }

    /**
     * POST  /api/user-facility-departments : Create a new UserFacilityDepartment.
     */
    @PostMapping
    public ResponseEntity<UserFacilityDepartmentResponseVM> createUserFacilityDepartment(@RequestBody UserFacilityDepartmentCreateVM vm) throws URISyntaxException {
        log.debug("REST request to create UserFacilityDepartment : {}", vm);
        UserFacilityDepartmentResponseVM result = userFacilityDepartmentService.createUserFacilityDepartment(vm);
        return ResponseEntity.created(new URI("/api/user-facility-departments/" + result.id())).body(result);
    }

    /**
     * PATCH  /api/user-facility-departments/{id}/toggle : Toggle active status.
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleActiveStatus(@PathVariable Long id) {
        log.debug("REST request to toggle active status for UserFacilityDepartment : {}", id);
        userFacilityDepartmentService.toggleActiveStatus(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET  /api/user-facility-departments/user/{userId} : Get all departments linked to a user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserFacilityDepartmentResponseVM>> getByUser(@PathVariable Long userId) {
        log.debug("REST request to get UserFacilityDepartments by userId : {}", userId);
        List<UserFacilityDepartmentResponseVM> result = userFacilityDepartmentService.getUserFacilityDepartmentsByUser(userId);
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /api/user-facility-departments/exists : Check if a link exists.
     */
    @GetMapping("/exists")
    public ResponseEntity<Boolean> exists(@RequestParam Long facilityId, @RequestParam Long userId, @RequestParam Long departmentId) {
        log.debug("REST request to check existence of UserFacilityDepartment (facilityId={}, userId={}, departmentId={})",
                facilityId, userId, departmentId);
        boolean exists = userFacilityDepartmentService.exists(facilityId, userId, departmentId);
        return ResponseEntity.ok(exists);
    }
}
