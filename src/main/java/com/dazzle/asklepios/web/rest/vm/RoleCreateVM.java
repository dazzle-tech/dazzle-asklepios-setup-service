package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Role;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * View Model for creating/updating a Role via REST.
 */
public record RoleCreateVM(
        @NotNull String name,
        @NotNull String type,
        @NotNull Long facilityId
) implements Serializable {

        public static RoleCreateVM ofEntity(Role role) {
                return new RoleCreateVM(
                        role.getName(),
                        role.getType(),
                        role.getFacility() != null ? role.getFacility().getId() : null
                );
        }
}
