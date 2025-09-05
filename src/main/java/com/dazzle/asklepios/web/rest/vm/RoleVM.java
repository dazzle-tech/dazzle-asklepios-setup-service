package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 *View Model for creating/updating a Role via REST.
 */
@Getter
@ToString
@Builder
public class RoleVM implements Serializable {

        @NotNull
        private final String name;

        @NotNull
        private final String type;

        @NotNull
        private final Long facilityId;

        public static RoleVM ofEntity(Role role) {
                return RoleVM.builder()
                        .name(role.getName())
                        .type(role.getType())
                        .facilityId(role.getFacilityId())
                        .build();
        }
}
