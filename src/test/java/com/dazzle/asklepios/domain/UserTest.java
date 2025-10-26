package com.dazzle.asklepios.domain;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void testBuilderAndGetters() {
        User user = User.builder()
                .id(1L)
                .login("testuser")
                .activated(true)
                .build();

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getLogin()).isEqualTo("testuser");
        assertThat(user.isActivated()).isTrue();
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = User.builder()
                .id(2L)
                .login("user1")
                .activated(false)
                .build();

        User user2 = User.builder()
                .id(2L)
                .login("user1")
                .activated(false)
                .build();

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void testSerialization() throws Exception {
        User user = User.builder()
                .id(3L)
                .login("serializedUser")
                .activated(true)
                .build();

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(user);

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        User deserialized = (User) in.readObject();

        assertThat(deserialized.getId()).isEqualTo(3L);
        assertThat(deserialized.getLogin()).isEqualTo("serializedUser");
        assertThat(deserialized.isActivated()).isTrue();
    }
}
