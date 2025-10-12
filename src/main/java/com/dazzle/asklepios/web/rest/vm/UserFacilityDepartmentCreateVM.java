package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.UserFacilityDepartment;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

/**
 * View Model for creating a UserFacilityDepartment via REST.
 **/
public record UserFacilityDepartmentCreateVM(
        @NotNull Long userId,
        @NotNull Long departmentId,
        Boolean isActive,
        String createdBy,
        Instant createdDate
) implements Serializable {

    public static UserFacilityDepartmentCreateVM ofEntity(UserFacilityDepartment entity) {
        return new UserFacilityDepartmentCreateVM(
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getDepartment() != null ? entity.getDepartment().getId() : null,
                entity.getIsActive(),
                entity.getCreatedBy(),
                entity.getCreatedDate()
        );
    }
}
