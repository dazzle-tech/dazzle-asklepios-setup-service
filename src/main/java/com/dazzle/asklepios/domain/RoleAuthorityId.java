package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class RoleAuthorityId implements Serializable {

    @Column(name = "role_id")
    private Long roleId;

    @NotNull
    @Column(name = "authority_name", length = 100, nullable = false)
    private String authorityName;

}
