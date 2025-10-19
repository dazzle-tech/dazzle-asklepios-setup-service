package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceTest {

    @Test
    void testBuilderAndGetters_WithFacility() {
        Instant now = Instant.now();

        Facility facility = new Facility();
        facility.setId(10L);
        facility.setName("Main Clinic");

        Service svc = Service.builder()
                .id(1001L)
                .name("MRI Scan")
                .abbreviation("MRI")
                .code("MRI-01")
                .category(ServiceCategory.CONSULTATION)
                .price(new BigDecimal("199.99"))
                .currency(Currency.USD)
                .isActive(true)
                .createdBy("tester")
                .createdDate(now)
                .lastModifiedBy("admin")
                .lastModifiedDate(now)
                .facility(facility)  // ✅ added facility
                .build();

        assertThat(svc.getId()).isEqualTo(1001L);
        assertThat(svc.getName()).isEqualTo("MRI Scan");
        assertThat(svc.getAbbreviation()).isEqualTo("MRI");
        assertThat(svc.getCode()).isEqualTo("MRI-01");
        assertThat(svc.getCategory()).isEqualTo(ServiceCategory.CONSULTATION);
        assertThat(svc.getPrice()).isEqualByComparingTo("199.99");
        assertThat(svc.getCurrency()).isEqualTo(Currency.USD);
        assertThat(svc.getIsActive()).isTrue();
        assertThat(svc.getCreatedBy()).isEqualTo("tester");
        assertThat(svc.getCreatedDate()).isEqualTo(now);
        assertThat(svc.getLastModifiedBy()).isEqualTo("admin");
        assertThat(svc.getLastModifiedDate()).isEqualTo(now);

        // ✅ Facility checks
        assertThat(svc.getFacility()).isNotNull();
        assertThat(svc.getFacility().getId()).isEqualTo(10L);
        assertThat(svc.getFacility().getName()).isEqualTo("Main Clinic");
    }

    @Test
    void testEqualsAndHashCode_ById() {
        Facility f1 = new Facility();
        f1.setId(11L);
        f1.setName("Alpha Center");

        Facility f2 = new Facility();
        f2.setId(11L);
        f2.setName("Alpha Center");

        Service a = Service.builder()
                .id(2002L)
                .name("X-Ray")
                .code("XR-01")
                .currency(Currency.USD)
                .isActive(true)
                .createdBy("tester")
                .facility(f1)
                .build();

        Service b = Service.builder()
                .id(2002L)
                .name("X-Ray")
                .code("XR-01")
                .currency(Currency.USD)
                .isActive(true)
                .createdBy("tester")
                .facility(f2)
                .build();

        // ✅ Same ID should still be equal even if facility is same or different instance
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void testSerialization_WithFacility() throws Exception {
        Facility facility = new Facility();
        facility.setId(20L);
        facility.setName("Downtown Branch");

        Service svc = Service.builder()
                .id(3003L)
                .name("Ultrasound")
                .code("US-01")
                .currency(Currency.USD)
                .isActive(true)
                .createdBy("tester")
                .facility(facility)
                .build();

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(svc);
        }

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        Service deserialized;
        try (ObjectInputStream in = new ObjectInputStream(bis)) {
            deserialized = (Service) in.readObject();
        }

        assertThat(deserialized.getName()).isEqualTo("Ultrasound");
        assertThat(deserialized.getCode()).isEqualTo("US-01");
        assertThat(deserialized.getId()).isEqualTo(3003L);

        // ✅ Facility also serialized correctly
        assertThat(deserialized.getFacility()).isNotNull();
        assertThat(deserialized.getFacility().getId()).isEqualTo(20L);
        assertThat(deserialized.getFacility().getName()).isEqualTo("Downtown Branch");
    }
}
