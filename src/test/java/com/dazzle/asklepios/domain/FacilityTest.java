package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FacilityTest {

    @Test
    void testBuilderAndGetters() {
        Facility facility = Facility.builder()
                .id(1001L)
                .name("Main General Hospital")
                .type(FacilityType.HOSPITAL)
                .code("MGH001")
                .registrationDate(LocalDate.of(2020, 5, 20))
                .emailAddress("info@mgh.example")
                .phone1("+1-555-111-1111")
                .phone2("+1-555-222-2222")
                .fax("+1-555-333-3333")
                // addressId is optional → not set here
                .defaultCurrency(Currency.USD)
                .isActive(true)
                .build();

        assertThat(facility.getId()).isEqualTo(1001L);
        assertThat(facility.getName()).isEqualTo("Main General Hospital");
        assertThat(facility.getType()).isEqualTo(FacilityType.HOSPITAL);
        assertThat(facility.getCode()).isEqualTo("MGH001");
        assertThat(facility.getRegistrationDate()).isEqualTo(LocalDate.of(2020, 5, 20));
        assertThat(facility.getEmailAddress()).isEqualTo("info@mgh.example");
        assertThat(facility.getPhone1()).isEqualTo("+1-555-111-1111");
        assertThat(facility.getPhone2()).isEqualTo("+1-555-222-2222");
        assertThat(facility.getFax()).isEqualTo("+1-555-333-3333");
        assertThat(facility.getAddressId()).isNull();
        assertThat(facility.getDefaultCurrency()).isEqualTo(Currency.USD);
        assertThat(facility.getIsActive()).isTrue();
    }

    @Test
    void testEqualsAndHashCode() {
        Facility f1 = Facility.builder()
                .id(2002L)
                .name("Clinic A")
                .type(FacilityType.CLINIC)
                .code("CLIN-A")
                .defaultCurrency(Currency.USD)
                .isActive(true)
                .build();

        Facility f2 = Facility.builder()
                .id(2002L)
                .name("Clinic A")
                .type(FacilityType.CLINIC)
                .code("CLIN-A")
                .defaultCurrency(Currency.USD)
                .isActive(true)
                .build();

        assertThat(f1).isEqualTo(f2);
        assertThat(f1.hashCode()).isEqualTo(f2.hashCode());
    }

    @Test
    void testSerialization() throws Exception {
        Facility facility = Facility.builder()
                .id(3003L)
                .name("Regional Center")
                .type(FacilityType.HOSPITAL)
                .code("RC-01")
                .defaultCurrency(Currency.USD)
                .isActive(true)
                .build();

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(facility);
        }

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        Facility deserialized;
        try (ObjectInputStream in = new ObjectInputStream(bis)) {
            deserialized = (Facility) in.readObject();
        }

        assertThat(deserialized.getId()).isEqualTo(3003L);
        assertThat(deserialized.getName()).isEqualTo("Regional Center");
        assertThat(deserialized.getCode()).isEqualTo("RC-01");
    }
}
