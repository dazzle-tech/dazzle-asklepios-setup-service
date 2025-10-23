package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.UserDepartment;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

/**
 * View Model for creating a UserFacilityDepartment via REST.
 **/
public record UserDepartmentCreateVM(
        @NotNull Long userId,
        @NotNull Long departmentId,
        Boolean isActive,
        String createdBy,
        Instant createdDate
) implements Serializable {

    public static UserDepartmentCreateVM ofEntity(UserDepartment entity) {
        return new UserDepartmentCreateVM(
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getDepartment() != null ? entity.getDepartment().getId() : null,
                entity.getIsActive(),
                entity.getCreatedBy(),
                entity.getCreatedDate()
        );
    }
}