package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Role;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * View Model for creating/updating a Role via REST.
 */
public record RoleVM(
        @NotNull String name,
        @NotNull String type,
        @NotNull Long facilityId
) implements Serializable {

        public static RoleVM ofEntity(Role role) {
                return new RoleVM(
                        role.getName(),
                        role.getType(),
                        role.getFacility() != null ? role.getFacility().getId() : null
                );
        }
}
