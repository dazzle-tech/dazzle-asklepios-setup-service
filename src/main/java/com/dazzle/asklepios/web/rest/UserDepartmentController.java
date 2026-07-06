package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.UserDepartment;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.service.UserDepartmentService;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.userDepartments.UserDepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.userDepartments.UserDepartmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.userDepartments.UserDepartmentTogglesVM;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        List<UserDepartmentResponseVM> result = userDepartmentService.getUserDepartmentsByUser(userId);
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
        log.warn(">>> ENTER /active endpoint, userId={}", userId);

        Long facilityId = SecurityUtils.getCurrentUserFacility()
                .orElseThrow(() -> {
                    log.error(">>> Missing mandatory claim 'tenant' in JWT for userId={}", userId);
                    return new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Missing mandatory claim 'tenant' in JWT."
                    );
                });

        log.warn(">>> Resolved facilityId from JWT = {}", facilityId);

        List<UserDepartmentResponseVM> result = userDepartmentService
                .getActiveUserDepartmentsByUserInFacility(userId, facilityId);

        log.warn(">>> Service returned {} active departments", result != null ? result.size() : null);

        if (result != null) {
            result.forEach(item -> log.warn(
                    ">>> item: id={}, userId={}, facilityId={}, facilityName={}, departmentId={}, departmentName={}, isActive={}, isDefault={}",
                    item.id(),
                    item.userId(),
                    item.facilityId(),
                    item.facilityName(),
                    item.departmentId(),
                    item.departmentName(),
                    item.isActive(),
                    item.isDefault()
            ));
        }

        log.warn(">>> EXIT /active endpoint");

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

    /**
     * GET /api/setup/user-departments/user/full-name?login= : Get user's full name by login.
     */
    @GetMapping("/user-departments/user/full-name")
    public ResponseEntity<String> getFullNameByLogin(@RequestParam String login) {
        log.debug("REST request to get full name by login={}", login);
        String fullName = userDepartmentService.getFullNameByLogin(login);
        log.debug("REST response full name for login={} -> {}", login, fullName);
        return ResponseEntity.ok(fullName);
    }

    /**
     * GET /api/setup/user-departments/user/id?login= : Get user's id by login.
     */
    @GetMapping("/user-departments/user/id")
    public ResponseEntity<Long> getIdByLogin(@RequestParam String login) {
        log.debug("REST request to get id by login={}", login);
        Long userId = userDepartmentService.getIdByLogin(login);
        return ResponseEntity.ok(userId);
    }

    /**
     * GET /api/setup/user-departments/user?login= : Get user's  by login.
     */
    @GetMapping("/user-departments/user")
    public ResponseEntity<User> getUserByLogin(@RequestParam String login) {
        log.debug("REST request to get user by login={}", login);
        User user = userDepartmentService.getUserByLogin(login);
        return ResponseEntity.ok(user);
    }

    /**
     * GET /api/setup/user-departments/user/by-user-id?userId= : Get user's  by id.
     */
    @GetMapping("/user-departments/user/by-user-id")
    public ResponseEntity<User> getUserById(@RequestParam Long userId) {
        log.debug("REST request to get user by id={}", userId);
        User user = userDepartmentService.getUserById(userId);
        return ResponseEntity.ok(user);
    }


    /**
     * PATCH /api/setup/user-departments/{id}/toggles :
     * Update only the toggle fields (isDefault, appointmentBookingAllowed).
     */
    @PatchMapping("/user-departments/{id}/toggles")
    public ResponseEntity<UserDepartment> updateToggles(@PathVariable Long id, @RequestBody UserDepartmentTogglesVM vm) {
        log.debug("REST request to update toggles for UserDepartment id={} : {}", id, vm);
        UserDepartment result = userDepartmentService.updateToggles(id, vm);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user-department/department/{departmentId}/users")
    public ResponseEntity<List<User>> getUsersForDepartments(@PathVariable Long departmentId) {
        log.debug("REST request to get users for department");

        List<User> result = userDepartmentService.getUserDepartmentsForDepartment(departmentId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/user-department/department/{departmentId}/physician-users")
    public ResponseEntity<List<User>> getPhysicianUsersForDepartments(@PathVariable Long departmentId) {
        log.debug("REST request to get physician users for department");

        List<User> result = userDepartmentService.getPhysicianUserDepartmentsForDepartment(departmentId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/user-department/department/internalJob/{departmentId}/users")
    public ResponseEntity<List<User>> getUsersForDepartmentsInternal(@PathVariable Long departmentId) {
        log.debug("REST request to get users for department");

        List<User> result = userDepartmentService.getUserDepartmentsForDepartment(departmentId);

        return ResponseEntity.ok(result);
    }

}
