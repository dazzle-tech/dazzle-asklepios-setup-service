package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Role;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * View Model for creating/updating a Role via REST.
 */
public record RoleUpdateVM(
        @NotNull Long id,
        String name,
        String type,
        Long facilityId
) implements Serializable {

        public static RoleUpdateVM ofEntity(Role role) {
                return new RoleUpdateVM(
                        role.getId(),
                        role.getName(),
                        role.getType(),
                        role.getFacility() != null ? role.getFacility().getId() : null
                );
        }
}
