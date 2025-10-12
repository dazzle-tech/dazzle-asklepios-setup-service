package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.UserFacilityDepartment;
import java.io.Serializable;

/**
 * View Model for reading a UserFacilityDepartment via REST.
 */
public record UserFacilityDepartmentResponseVM(
        Long id,
        Long userId,
        Long facilityId,
        Long departmentId,
        Boolean isActive
) implements Serializable {

    public static UserFacilityDepartmentResponseVM ofEntity(UserFacilityDepartment entity) {
        return new UserFacilityDepartmentResponseVM(
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
