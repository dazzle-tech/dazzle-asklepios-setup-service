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
    void testBuilderAndGetters() {
        Instant now = Instant.now();

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
                .build();

        assertThat(svc.getId()).isEqualTo(1001L);
        assertThat(svc.getName()).isEqualTo("MRI Scan");
        assertThat(svc.getAbbreviation()).isEqualTo("MRI");
        assertThat(svc.getCode()).isEqualTo("MRI-01");
        assertThat(svc.getCategory()).isEqualTo(ServiceCategory.CONSULTATION);
        assertThat(svc.getPrice()).isNotNull();
        assertThat(svc.getPrice()).isEqualByComparingTo("199.99");
        assertThat(svc.getCurrency()).isEqualTo(Currency.USD);
        assertThat(svc.getIsActive()).isTrue();
        assertThat(svc.getCreatedBy()).isEqualTo("tester");
        assertThat(svc.getCreatedDate()).isEqualTo(now);
        assertThat(svc.getLastModifiedBy()).isEqualTo("admin");
        assertThat(svc.getLastModifiedDate()).isEqualTo(now);
    }

    @Test
    void testEqualsAndHashCode_ById() {
        Service a = Service.builder()
                .id(2002L)
                .name("X-Ray")
                .code("XR-01")
                .currency(Currency.USD)
                .isActive(true)
                .createdBy("tester")
                .build();

        Service b = Service.builder()
                .id(2002L)
                .name("X-Ray")
                .code("XR-01")
                .currency(Currency.USD)
                .isActive(true)
                .createdBy("tester")
                .build();

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void testSerialization() throws Exception {
        Service svc = Service.builder()
                .id(3003L)
                .name("Ultrasound")
                .code("US-01")
                .currency(Currency.USD)
                .isActive(true)
                .createdBy("tester")
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
    }
}
