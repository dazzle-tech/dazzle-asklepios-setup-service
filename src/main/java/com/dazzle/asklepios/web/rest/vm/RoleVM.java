package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * View Model for creating/updating a Role via REST.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RoleVM {

        @NotNull
        private String name;

        @NotNull
        private String type;

        @NotNull
        private Long facilityId;


        public static RoleVM from(Role role) {
                return RoleVM.builder()
                        .name(role.getName())
                        .type(role.getType())
                        .facilityId(role.getFacilityId())
                        .build();
        }
}
