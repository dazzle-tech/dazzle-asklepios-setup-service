package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Role;
import java.io.Serializable;

/**
 * View Model for reading ROle
 */
public record RoleResponseVM(
        Long id,
        String name,
        String type,
        Long facilityId
) implements Serializable {

        public static RoleResponseVM ofEntity(Role role) {
                return new RoleResponseVM(
                        role.getId(),
                        role.getName(),
                        role.getType(),
                        role.getFacility() != null ? role.getFacility().getId() : null
                );
        }
}
