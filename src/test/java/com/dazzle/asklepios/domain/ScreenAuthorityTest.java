package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


import static org.assertj.core.api.Assertions.assertThat;

class ScreenAuthorityTest {

    @Test
    void testBuilderAndGetters() {
        ScreenAuthority authority = ScreenAuthority.builder()
                .id(1L)
                .screen(Screen.SCHEDULING_SCREEN)
                .operation(Operation.VIEW)
                .authorityName("SCHEDULING_VIEW")
                .build();

        assertThat(authority.getId()).isEqualTo(1L);
        assertThat(authority.getScreen()).isEqualTo(Screen.SCHEDULING_SCREEN);
        assertThat(authority.getOperation()).isEqualTo(Operation.VIEW);
        assertThat(authority.getAuthorityName()).isEqualTo("SCHEDULING_VIEW");
    }

    @Test
    void testEqualsAndHashCode() {
        ScreenAuthority auth1 = ScreenAuthority.builder()
                .id(2L)
                .screen(Screen.USERS)
                .operation(Operation.EDIT)
                .authorityName("USERS_EDIT")
                .build();

        ScreenAuthority auth2 = ScreenAuthority.builder()
                .id(2L)
                .screen(Screen.USERS)
                .operation(Operation.EDIT)
                .authorityName("USERS_EDIT")
                .build();

        assertThat(auth1).isEqualTo(auth2);
        assertThat(auth1.hashCode()).isEqualTo(auth2.hashCode());
    }

    @Test
    void testSerialization() throws Exception {
        ScreenAuthority authority = ScreenAuthority.builder()
                .id(3L)
                .screen(Screen.LEDGER_ACCOUNT)
                .operation(Operation.VIEW)
                .authorityName("LEDGER_VIEW")
                .build();

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(authority);

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        ScreenAuthority deserialized = (ScreenAuthority) in.readObject();

        assertThat(deserialized.getId()).isEqualTo(3L);
        assertThat(deserialized.getScreen()).isEqualTo(Screen.LEDGER_ACCOUNT);
        assertThat(deserialized.getOperation()).isEqualTo(Operation.VIEW);
        assertThat(deserialized.getAuthorityName()).isEqualTo("LEDGER_VIEW");
    }
}
