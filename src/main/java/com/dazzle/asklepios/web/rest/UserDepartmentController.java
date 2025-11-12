package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.UserDepartment;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.service.UserDepartmentService;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.userDepartments.UserDepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.userDepartments.UserDepartmentResponseVM;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
     * POST  /api/setup/user-departments : Create a new UserFacilityDepartment.
     */
    @PostMapping("/user-departments")
    public ResponseEntity<UserDepartment> createUserFacilityDepartment(@RequestBody UserDepartmentCreateVM vm) throws URISyntaxException {
        log.debug("REST request to create UserFacilityDepartment : {}", vm);
        UserDepartment result = userDepartmentService.createUserDepartment(vm);
        return ResponseEntity.created(new URI("/api/user-facility-departments/" + result.getId())).body(result);
    }

    /**
     * GET  /api/setup/user-departments/user/{userId} : Get all departments linked to a user.
     */
    @GetMapping("/user-departments/user/{userId}")
    public ResponseEntity<List<UserDepartmentResponseVM>> getByUser(@PathVariable Long userId) {
        log.debug("REST request to get UserFacilityDepartments by userId : {}", userId);
        List<UserDepartmentResponseVM> result = userDepartmentService.getUserDepartmentsByUser(userId)
                .stream()
                .map(UserDepartmentResponseVM::ofEntity)
                .toList();
        ;
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /api/setup/user-departments/exists : Check if a link exists.
     */
    @GetMapping("/user-departments/exists")
    public ResponseEntity<Boolean> exists(@RequestParam Long userId, @RequestParam Long departmentId) {
        log.debug("REST request to check existence of UserFacilityDepartment ( userId={}, departmentId={})", userId, departmentId);
        boolean exists = userDepartmentService.exists(userId, departmentId);
        return ResponseEntity.ok(exists);
    }

    /**
     * @param id DELETE /api/setup/user-departments/{id}: Hard delete for user department
     */
    @DeleteMapping("/user-departments/{id}")
    public ResponseEntity<Void> deleteUserDepartment(@PathVariable Long id) {
        log.debug("REST request to hard delete UserDepartment: {}", id);
        try {
            userDepartmentService.hardDelete(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            throw new NotFoundAlertException("UserDepartment not found", "userDepartment", "id_notfound");
        } catch (IllegalArgumentException e) {
            throw new BadRequestAlertException("Invalid UserDepartment ID", "userDepartment", "id_invalid");
        }
    }

    /**
     * GET /api/setup/user-departments/user/{userId}/active :
     * Get active departments for a user in the requested facility.
     */
    @GetMapping("/user-departments/user/{userId}/active")
    public ResponseEntity<List<UserDepartmentResponseVM>> getActiveByUserInFacility(@PathVariable Long userId) {
        Long facilityId = SecurityUtils.getCurrentUserFacility()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing mandatory claim 'tenant' in JWT."));

        log.debug("REST request to get active UserDepartments by userId={} facilityId={}", userId, facilityId);
        List<UserDepartmentResponseVM> result = userDepartmentService
                .getActiveUserDepartmentsByUserInFacility(userId, facilityId)
                .stream()
                .map(UserDepartmentResponseVM::ofEntity)
                .toList();
        return ResponseEntity.ok(result);
    }


    /**
     * GET /api/setup/user-departments/user/{userId}/default :
     * Get the default department for a user within a facility.
     */
    @GetMapping("/user-departments/user/{userId}/default")
    public ResponseEntity<UserDepartmentResponseVM> getDefaultByUserAndFacility(@PathVariable Long userId) {
        Long facilityId = SecurityUtils.getCurrentUserFacility()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing mandatory claim 'tenant' in JWT."));

        log.debug("REST request to get DEFAULT UserDepartment by userId={} facilityId={}", userId, facilityId);
        return userDepartmentService.getDefaultUserDepartmentByFacility(userId, facilityId)
                .map(ud -> ResponseEntity.ok(UserDepartmentResponseVM.ofEntity(ud)))
                .orElse(ResponseEntity.noContent().build());
    }

}