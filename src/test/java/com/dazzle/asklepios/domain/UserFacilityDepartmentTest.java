package com.dazzle.asklepios.domain;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserFacilityDepartmentTest {

    private Facility facility(Long id) {
        Facility f = new Facility();
        f.setId(id);
        return f;
    }

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

        UserFacilityDepartment ufd = UserFacilityDepartment.builder()
                .facility(facility(1L))
                .user(user(2L))
                .department(department(3L))
                .createdBy("tester")
                .createdDate(now)
                .lastModifiedBy("mod")
                .lastModifiedDate(now.plusSeconds(60))
                .isActive(Boolean.TRUE)
                .build();

        assertThat(ufd.getFacility()).isNotNull();
        assertThat(ufd.getFacility().getId()).isEqualTo(1L);
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
        UserFacilityDepartment ufd = new UserFacilityDepartment();
        assertThat(ufd.getIsActive()).isTrue();
        assertThat(ufd.getId()).isNull();
        assertThat(ufd.getFacility()).isNull();
        assertThat(ufd.getUser()).isNull();
        assertThat(ufd.getDepartment()).isNull();
    }

    @Test
    void testEqualsAndHashCode() {
        UserFacilityDepartment a = new UserFacilityDepartment();
        a.setId(100L);
        UserFacilityDepartment b = new UserFacilityDepartment();
        b.setId(100L);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void testEqualsAndHashCodeNotEqual() {
        UserFacilityDepartment a = new UserFacilityDepartment();
        a.setId(100L);
        UserFacilityDepartment b = new UserFacilityDepartment();
        b.setId(101L);

        assertThat(a).isNotEqualTo(b);
        assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
    }

    @Test
    void TestEqualsIDs() {
        UserFacilityDepartment a = new UserFacilityDepartment(); // id null
        UserFacilityDepartment b = new UserFacilityDepartment();
        b.setId(1L);

        assertThat(a).isNotEqualTo(b);
        assertThat(b).isNotEqualTo(a);
    }
    @Test
    void testSerialization() throws Exception {
        UserFacilityDepartment original = UserFacilityDepartment.builder()
                .facility(facility(1L))
                .user(user(2L))
                .department(department(3L))
                .createdBy("ser")
                .createdDate(Instant.parse("2023-06-01T12:00:00Z"))
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

        UserFacilityDepartment copy;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            copy = (UserFacilityDepartment) ois.readObject();
        }

        assertThat(copy.getId()).isEqualTo(777L);
        assertThat(copy.getFacility().getId()).isEqualTo(1L);
        assertThat(copy.getUser().getId()).isEqualTo(2L);
        assertThat(copy.getDepartment().getId()).isEqualTo(3L);
        assertThat(copy.getCreatedBy()).isEqualTo("ser");
        assertThat(copy.getLastModifiedBy()).isEqualTo("upd");
        assertThat(copy.getIsActive()).isTrue();
    }
}
