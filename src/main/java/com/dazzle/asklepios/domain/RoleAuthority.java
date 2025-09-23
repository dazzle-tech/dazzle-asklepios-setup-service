package com.dazzle.asklepios.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "role_authority")
public class RoleAuthority {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private RoleAuthorityId id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private Role role;
}
