package com.dazzle.asklepios.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTest {

    @Test
    void testBuilderAndGetters() {

        UserRole.UserRoleId userRoleId = new UserRole.UserRoleId(1L, 10L);


        UserRole userRole = UserRole.builder()
                .id(userRoleId)
                .build();


        assertThat(userRole.getId()).isEqualTo(userRoleId);
        assertThat(userRole.getId().getUserId()).isEqualTo(1L);
        assertThat(userRole.getId().getRoleId()).isEqualTo(10L);
    }

    @Test
    void testEqualsAndHashCode() {
        UserRole.UserRoleId id1 = new UserRole.UserRoleId(2L, 20L);
        UserRole.UserRoleId id2 = new UserRole.UserRoleId(2L, 20L);

        UserRole ur1 = UserRole.builder().id(id1).build();
        UserRole ur2 = UserRole.builder().id(id2).build();

        assertThat(ur1).isEqualTo(ur2);
        assertThat(ur1.hashCode()).isEqualTo(ur2.hashCode());
    }
}
