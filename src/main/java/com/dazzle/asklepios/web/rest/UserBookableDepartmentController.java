package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.UserBookableDepartment;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.service.UserBookableDepartmentService;
import com.dazzle.asklepios.service.dto.userBookableDepartment.UserBookableDepartmentCreateVM;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.userBookableDepartments.UserBookableDepartmentResponseVM;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class UserBookableDepartmentController {

    private final Logger log = LoggerFactory.getLogger(UserBookableDepartmentController.class);
    private final UserBookableDepartmentService userBookableDepartmentService;

    public UserBookableDepartmentController(UserBookableDepartmentService userBookableDepartmentService) {
        this.userBookableDepartmentService = userBookableDepartmentService;
    }

    /**
     * POST  /api/setup/user-bookable-departments : Create a new UserBookableDepartment.
     */
    @PostMapping("/user-bookable-departments")
    public ResponseEntity<UserBookableDepartment> createUserBookableDepartment(@RequestBody UserBookableDepartmentCreateVM vm) throws URISyntaxException {
        log.debug("REST request to create UserFacilityDepartment : {}", vm);
        UserBookableDepartment result = userBookableDepartmentService.createUserBookableDepartment(vm);
        return ResponseEntity.created(new URI("/api/user-bookable-departments/" + result.getId())).body(result);
    }


    /**
     * GET  /api/setup/user-bookable-departments/exists : Check if a link exists.
     */
    @GetMapping("/user-bookable-departments/exists")
    public ResponseEntity<Boolean> exists(@RequestParam Long userId, @RequestParam Long departmentId) {
        log.debug("REST request to check existence of UserBookableDepartment ( userId={}, departmentId={})", userId, departmentId);
        boolean exists = userBookableDepartmentService.exists(userId, departmentId);
        return ResponseEntity.ok(exists);
    }

    /**
     * @param id DELETE /api/setup/user-bookable-departments/{id}: Hard delete for user bookable department
     */
    @DeleteMapping("/user-bookable-departments/{id}")
    public ResponseEntity<Void> deleteUserDepartment(@PathVariable Long id) {
        log.debug("REST request to hard delete UserBookableDepartment: {}", id);
        try {
            userBookableDepartmentService.hardDelete(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            throw new NotFoundAlertException("UserBookableDepartment not found", "userBookableDepartment", "id_notfound");
        } catch (IllegalArgumentException e) {
            throw new BadRequestAlertException("Invalid UserBookableDepartment ID", "userBookableDepartment", "id_invalid");
        }
    }

    /**
     * GET /api/setup/user-bookable-departments/user/{userId}/active :
     * Get active bookable departments for a user in the requested facility.
     */
    @GetMapping("/user-bookable-departments/user/{userId}/active")
    public ResponseEntity<List<UserBookableDepartmentResponseVM>> getActiveByUserInFacility(@PathVariable Long userId) {
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

        List<UserBookableDepartmentResponseVM> result = userBookableDepartmentService
                .getUserBookableDepartmentsByUserInFacility(userId, facilityId);

        log.warn(">>> Service returned {} active bookable departments", result != null ? result.size() : null);

        if (result != null) {
            result.forEach(item -> log.warn(
                    ">>> item: id={}, userId={}, facilityId={}, facilityName={}, departmentId={}, departmentName={}",
                    item.id(),
                    item.userId(),
                    item.facilityId(),
                    item.facilityName(),
                    item.departmentId(),
                    item.departmentName()
            ));
        }


        return ResponseEntity.ok(result);
    }

}
