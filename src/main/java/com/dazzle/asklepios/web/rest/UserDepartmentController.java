package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.UserDepartmentService;
import com.dazzle.asklepios.web.rest.vm.UserDepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.UserDepartmentResponseVM;
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
@RequestMapping("/api/setup")
public class UserDepartmentController {

    private final Logger log = LoggerFactory.getLogger(UserDepartmentController.class);
    private final UserDepartmentService userDepartmentService;

    public UserDepartmentController(UserDepartmentService userDepartmentService) {
        this.userDepartmentService = userDepartmentService;
    }

    /**
     * POST  /api/user-departments : Create a new UserFacilityDepartment.
     */
    @PostMapping("/user-departments")
    public ResponseEntity<UserDepartmentResponseVM> createUserFacilityDepartment(@RequestBody UserDepartmentCreateVM vm) throws URISyntaxException {
        log.debug("REST request to create UserFacilityDepartment : {}", vm);
        UserDepartmentResponseVM result = userDepartmentService.createUserDepartment(vm);
        return ResponseEntity.created(new URI("/api/user-facility-departments/" + result.id())).body(result);
    }

    /**
     * PATCH  /api/user-departments/{id}/toggle : Toggle active status.
     */
    @PatchMapping("/user-departments/{id}/toggle")
    public ResponseEntity<Void> toggleActiveStatus(@PathVariable Long id) {
        log.debug("REST request to toggle active status for UserFacilityDepartment : {}", id);
        userDepartmentService.toggleActiveStatus(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET  /api/user-departments/user/{userId} : Get all departments linked to a user.
     */
    @GetMapping("/user-departments/user/{userId}")
    public ResponseEntity<List<UserDepartmentResponseVM>> getByUser(@PathVariable Long userId) {
        log.debug("REST request to get UserFacilityDepartments by userId : {}", userId);
        List<UserDepartmentResponseVM> result = userDepartmentService.getUserDepartmentsByUser(userId);
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /api/user-departments/exists : Check if a link exists.
     */
    @GetMapping("/user-departments/exists")
    public ResponseEntity<Boolean> exists( @RequestParam Long userId, @RequestParam Long departmentId) {
        log.debug("REST request to check existence of UserFacilityDepartment ( userId={}, departmentId={})",userId, departmentId);
        boolean exists = userDepartmentService.exists(userId, departmentId);
        return ResponseEntity.ok(exists);
    }
}