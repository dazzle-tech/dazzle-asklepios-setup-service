package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void testBuilderAndGetters() {
        Facility facility = Facility.builder()
                .id(1L)
                .name("Main Hospital")
                .type(FacilityType.HOSPITAL)
                .code("HSP-001")
                .defaultCurrency(Currency.USD)
                .isActive(true)
                .build();

        Role role = Role.builder()
                .id(100L)
                .name("ADMIN")
                .type("SYSTEM")
                .facility(facility)
                .build();

        assertThat(role.getId()).isEqualTo(100L);
        assertThat(role.getName()).isEqualTo("ADMIN");
        assertThat(role.getType()).isEqualTo("SYSTEM");
        assertThat(role.getFacility()).isEqualTo(facility);
        // facilityId managed by JPA → not set in plain unit test
    }

    @Test
    void testEqualsAndHashCode() {
        Role r1 = Role.builder()
                .id(200L)
                .name("USER")
                .type("SYSTEM")
                .build();

        Role r2 = Role.builder()
                .id(200L)
                .name("USER")
                .type("SYSTEM")
                .build();

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void testFacilityRelationship() {
        Facility facility = Facility.builder()
                .id(2L)
                .name("Clinic A")
                .type(FacilityType.CLINIC)
                .code("CLN-002")
                .defaultCurrency(Currency.USD)
                .isActive(true)
                .build();

        Role role = Role.builder()
                .name("OPERATOR")
                .type("LOCAL")
                .facility(facility)
                .build();

        assertThat(role.getFacility()).isNotNull();
        assertThat(role.getFacility().getName()).isEqualTo("Clinic A");
    }
}
