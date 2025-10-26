package com.dazzle.asklepios.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleAuthorityIdTest {

    @Test
    void testBuilderAndGetters() {

        RoleAuthorityId id = RoleAuthorityId.builder()
                .roleId(1L)
                .authorityName("READ_PRIVILEGE")
                .build();


        assertThat(id.getRoleId()).isEqualTo(1L);
        assertThat(id.getAuthorityName()).isEqualTo("READ_PRIVILEGE");


        id.setRoleId(2L);
        id.setAuthorityName("WRITE_PRIVILEGE");

        assertThat(id.getRoleId()).isEqualTo(2L);
        assertThat(id.getAuthorityName()).isEqualTo("WRITE_PRIVILEGE");
    }

    @Test
    void testEqualsAndHashCode() {
        RoleAuthorityId id1 = RoleAuthorityId.builder()
                .roleId(1L)
                .authorityName("READ_PRIVILEGE")
                .build();

        RoleAuthorityId id2 = RoleAuthorityId.builder()
                .roleId(1L)
                .authorityName("READ_PRIVILEGE")
                .build();

        RoleAuthorityId id3 = RoleAuthorityId.builder()
                .roleId(2L)
                .authorityName("WRITE_PRIVILEGE")
                .build();


        assertThat(id1).isEqualTo(id2);
        assertThat(id1).isNotEqualTo(id3);


        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        assertThat(id1.hashCode()).isNotEqualTo(id3.hashCode());
    }
}
