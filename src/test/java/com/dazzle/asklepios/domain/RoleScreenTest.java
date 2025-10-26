package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


import static org.assertj.core.api.Assertions.assertThat;

class RoleScreenTest {

    @Test
    void testBuilderAndGetters() {
        Role role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        RoleScreenId roleScreenId = RoleScreenId.builder()
                .roleId(1L)
                .screen(Screen.SCHEDULING_SCREEN)
                .operation(Operation.VIEW)
                .build();

        RoleScreen roleScreen = RoleScreen.builder()
                .id(roleScreenId)
                .role(role)
                .build();

        assertThat(roleScreen.getId().getRoleId()).isEqualTo(1L);
        assertThat(roleScreen.getId().getScreen()).isEqualTo(Screen.SCHEDULING_SCREEN);
        assertThat(roleScreen.getId().getOperation()).isEqualTo(Operation.VIEW);
        assertThat(roleScreen.getRole().getId()).isEqualTo(1L);
        assertThat(roleScreen.getRole().getName()).isEqualTo("ADMIN");
    }

    @Test
    void testEqualsAndHashCode() {
        RoleScreenId id1 = RoleScreenId.builder()
                .roleId(2L)
                .screen(Screen.USERS)
                .operation(Operation.EDIT)
                .build();

        RoleScreenId id2 = RoleScreenId.builder()
                .roleId(2L)
                .screen(Screen.USERS)
                .operation(Operation.EDIT)
                .build();

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void testSerialization() throws Exception {
        Role role = Role.builder()
                .id(3L)
                .name("USER")
                .build();

        RoleScreenId id = RoleScreenId.builder()
                .roleId(3L)
                .screen(Screen.LEDGER_ACCOUNT)
                .operation(Operation.VIEW)
                .build();

        RoleScreen roleScreen = RoleScreen.builder()
                .id(id)
                .role(role)
                .build();

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(roleScreen);

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        RoleScreen deserialized = (RoleScreen) in.readObject();

        assertThat(deserialized.getId().getRoleId()).isEqualTo(3L);
        assertThat(deserialized.getId().getScreen()).isEqualTo(Screen.LEDGER_ACCOUNT);
        assertThat(deserialized.getId().getOperation()).isEqualTo(Operation.VIEW);
        assertThat(deserialized.getRole().getId()).isEqualTo(3L);
        assertThat(deserialized.getRole().getName()).isEqualTo("USER");
    }
}
