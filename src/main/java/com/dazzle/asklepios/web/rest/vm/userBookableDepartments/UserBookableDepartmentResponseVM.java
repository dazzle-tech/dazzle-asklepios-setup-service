package com.dazzle.asklepios.web.rest.vm.userBookableDepartments;

import com.dazzle.asklepios.domain.UserBookableDepartment;

import java.io.Serializable;

/**
 * View Model for reading a UserBookableDepartment via REST.
 */
public record UserBookableDepartmentResponseVM(
        Long id,
        Long userId,
        Long facilityId,
        String facilityName,
        Long departmentId,
        String departmentName
) implements Serializable {

    public static UserBookableDepartmentResponseVM ofEntity(UserBookableDepartment entity) {
        return new UserBookableDepartmentResponseVM(
                entity.getId(),
                entity.getUser() != null ? entity.getUser().getId() : null,

                entity.getDepartment() != null && entity.getDepartment().getFacility() != null
                        ? entity.getDepartment().getFacility().getId()
                        : null,

                entity.getDepartment() != null && entity.getDepartment().getFacility() != null
                        ? entity.getDepartment().getFacility().getName()
                        : null,

                entity.getDepartment() != null
                        ? entity.getDepartment().getId()
                        : null,

                entity.getDepartment() != null
                        ? entity.getDepartment().getName()
                        : null
        );
    }
}