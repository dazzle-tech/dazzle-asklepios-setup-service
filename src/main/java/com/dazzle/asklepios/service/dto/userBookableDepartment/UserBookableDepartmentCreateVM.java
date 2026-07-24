package com.dazzle.asklepios.service.dto.userBookableDepartment;

import com.dazzle.asklepios.domain.UserBookableDepartment;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * View Model for creating a UserBookableDepartment via REST.
 **/
public record UserBookableDepartmentCreateVM(
        @NotNull Long userId,
        @NotNull Long departmentId
) implements Serializable {

    public static UserBookableDepartmentCreateVM ofEntity(UserBookableDepartment entity) {
        return new UserBookableDepartmentCreateVM(
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getDepartment() != null ? entity.getDepartment().getId() : null
        );
    }
}