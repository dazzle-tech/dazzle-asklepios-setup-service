package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


import static org.assertj.core.api.Assertions.assertThat;

class RoleScreenIdTest {

    @Test
    void testBuilderAndGetters() {
        RoleScreenId id = RoleScreenId.builder()
                .roleId(1L)
                .screen(Screen.SCHEDULING_SCREEN)
                .operation(Operation.VIEW)
                .build();

        assertThat(id.getRoleId()).isEqualTo(1L);
        assertThat(id.getScreen()).isEqualTo(Screen.SCHEDULING_SCREEN);
        assertThat(id.getOperation()).isEqualTo(Operation.VIEW);
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
        RoleScreenId id = RoleScreenId.builder()
                .roleId(3L)
                .screen(Screen.LEDGER_ACCOUNT)
                .operation(Operation.VIEW)
                .build();

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(id);

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        RoleScreenId deserialized = (RoleScreenId) in.readObject();

        assertThat(deserialized.getRoleId()).isEqualTo(3L);
        assertThat(deserialized.getScreen()).isEqualTo(Screen.LEDGER_ACCOUNT);
        assertThat(deserialized.getOperation()).isEqualTo(Operation.VIEW);
    }
}
