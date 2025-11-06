package com.dazzle.asklepios.web.rest.vm.userDepartments;

import com.dazzle.asklepios.domain.UserDepartment;
import java.io.Serializable;

/**
 * View Model for reading a UserFacilityDepartment via REST.
 */
public record UserDepartmentResponseVM(
        Long id,
        Long userId,
        Long facilityId,
        Long departmentId,
        Boolean isActive
) implements Serializable {

    public static UserDepartmentResponseVM ofEntity(UserDepartment entity) {
        return new UserDepartmentResponseVM(
                entity.getId(),
                entity.getUser() != null ? entity.getUser().getId() : null,
                (entity.getDepartment() != null && entity.getDepartment().getFacility() != null)
                        ? entity.getDepartment().getFacility().getId()
                        : null,
                entity.getDepartment() != null ? entity.getDepartment().getId() : null,
                entity.getIsActive()
        );
    }
}