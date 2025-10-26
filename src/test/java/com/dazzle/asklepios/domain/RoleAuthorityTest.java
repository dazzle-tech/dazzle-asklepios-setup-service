package com.dazzle.asklepios.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RoleAuthorityTest {

    @Test
    void testBuilderAndGetters() {

        Role role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .type("SYSTEM")
                .build();

        RoleAuthorityId id = new RoleAuthorityId(1L, "READ_PRIVILEGE");

        RoleAuthority roleAuth = RoleAuthority.builder()
                .id(id)
                .role(role)
                .build();

        // Assertions
        assertThat(roleAuth.getId()).isEqualTo(id);
        assertThat(roleAuth.getRole()).isEqualTo(role);
        assertThat(roleAuth.getRole().getName()).isEqualTo("ADMIN");
    }

    @Test
    void testEqualsAndHashCode() {
        Role role = Role.builder()
                .id(2L)
                .name("USER")
                .type("SYSTEM")
                .build();

        RoleAuthorityId id1 = new RoleAuthorityId(2L, "WRITE_PRIVILEGE");
        RoleAuthorityId id2 = new RoleAuthorityId(2L, "WRITE_PRIVILEGE");

        RoleAuthority ra1 = RoleAuthority.builder().id(id1).role(role).build();
        RoleAuthority ra2 = RoleAuthority.builder().id(id2).role(role).build();

        assertThat(ra1).isEqualTo(ra2);
        assertThat(ra1.hashCode()).isEqualTo(ra2.hashCode());
    }
}
