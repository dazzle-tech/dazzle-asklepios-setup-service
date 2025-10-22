package com.dazzle.asklepios.domain;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserDepartmentTest {

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private Department department(Long id) {
        Department d = new Department();
        d.setId(id);
        return d;
    }

    @Test
    void testBuilderAndGetters() {
        Instant now = Instant.now();

        UserDepartment ufd = UserDepartment.builder()
                .user(user(2L))
                .department(department(3L))
                .isActive(Boolean.TRUE)
                .build();

        assertThat(ufd.getUser()).isNotNull();
        assertThat(ufd.getUser().getId()).isEqualTo(2L);
        assertThat(ufd.getDepartment()).isNotNull();
        assertThat(ufd.getDepartment().getId()).isEqualTo(3L);
        assertThat(ufd.getCreatedBy()).isEqualTo("tester");
        assertThat(ufd.getCreatedDate()).isEqualTo(now);
        assertThat(ufd.getLastModifiedBy()).isEqualTo("mod");
        assertThat(ufd.getLastModifiedDate()).isEqualTo(now.plusSeconds(60));
        assertThat(ufd.getIsActive()).isTrue();
    }

    @Test
    void noArgsConstructorAppliesFieldDefaults() {
        UserDepartment ufd = new UserDepartment();
        assertThat(ufd.getIsActive()).isTrue();
        assertThat(ufd.getId()).isNull();
        assertThat(ufd.getUser()).isNull();
        assertThat(ufd.getDepartment()).isNull();
    }

    @Test
    void testEqualsAndHashCode() {
        UserDepartment a = new UserDepartment();
        a.setId(100L);
        UserDepartment b = new UserDepartment();
        b.setId(100L);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void testEqualsAndHashCodeNotEqual() {
        UserDepartment a = new UserDepartment();
        a.setId(100L);
        UserDepartment b = new UserDepartment();
        b.setId(101L);

        assertThat(a).isNotEqualTo(b);
        assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
    }

    @Test
    void TestEqualsIDs() {
        UserDepartment a = new UserDepartment(); // id null
        UserDepartment b = new UserDepartment();
        b.setId(1L);

        assertThat(a).isNotEqualTo(b);
        assertThat(b).isNotEqualTo(a);
    }
    @Test
    void testSerialization() throws Exception {
        UserDepartment original = UserDepartment.builder()
                .user(user(2L))
                .department(department(3L))
                .isActive(true)
                .build();
        original.setId(777L);
        original.setLastModifiedBy("upd");
        original.setLastModifiedDate(Instant.parse("2023-06-02T12:00:00Z"));

        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
            oos.flush();
            bytes = baos.toByteArray();
        }

        UserDepartment copy;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            copy = (UserDepartment) ois.readObject();
        }

        assertThat(copy.getId()).isEqualTo(777L);
        assertThat(copy.getUser().getId()).isEqualTo(2L);
        assertThat(copy.getDepartment().getId()).isEqualTo(3L);
        assertThat(copy.getCreatedBy()).isEqualTo("ser");
        assertThat(copy.getLastModifiedBy()).isEqualTo("upd");
        assertThat(copy.getIsActive()).isTrue();
    }
}