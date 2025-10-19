package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ServiceItemsType;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceItemsTest {

    @Test
    void testBuilderAndGetters() {
        Instant now = Instant.now();

        // خدمة مرتبطة (FK) — نضبط فقط id وباقي الحقول ليست مطلوبة للاختبار
        Service svc = Service.builder()
                .id(11L)
                .name("Radiology")
                .code("RAD-001")
                .category(ServiceCategory.CONSULTATION)
                .currency(Currency.USD)
                .isActive(true)
                .createdBy("tester")
                .build();

        ServiceItems item = ServiceItems.builder()
                .id(1001L)
                .type(ServiceItemsType.DEPARTMENTS)  // عدّل القيمة حسب enum لديك
                .sourceId(55L)
                .service(svc)
                .createdBy("tester")
                .createdDate(now)
                .lastModifiedBy("admin")
                .lastModifiedDate(now)
                .isActive(true) // مهم: مع Lombok Builder يجب تمريره صراحةً
                .build();

        assertThat(item.getId()).isEqualTo(1001L);
        assertThat(item.getType()).isEqualTo(ServiceItemsType.DEPARTMENTS);
        assertThat(item.getSourceId()).isEqualTo(55L);

        // تحقق من FK
        assertThat(item.getService()).isNotNull();
        assertThat(item.getService().getId()).isEqualTo(11L);

        assertThat(item.getCreatedBy()).isEqualTo("tester");
        assertThat(item.getCreatedDate()).isEqualTo(now);
        assertThat(item.getLastModifiedBy()).isEqualTo("admin");
        assertThat(item.getLastModifiedDate()).isEqualTo(now);
        assertThat(item.getIsActive()).isTrue();
    }

    @Test
    void testEqualsAndHashCode_ById() {
        ServiceItems a = ServiceItems.builder()
                .id(2002L)
                .type(ServiceItemsType.DEPARTMENTS)
                .sourceId(77L)
                .createdBy("tester")
                .isActive(true)
                .build();

        ServiceItems b = ServiceItems.builder()
                .id(2002L)
                .type(ServiceItemsType.DEPARTMENTS)
                .sourceId(77L)
                .createdBy("tester")
                .isActive(true)
                .build();

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void testSerialization() throws Exception {
        Service svc = Service.builder()
                .id(33L)
                .name("Cardiology")
                .code("CAR-001")
                .currency(Currency.USD)
                .isActive(true)
                .createdBy("tester")
                .build();

        ServiceItems item = ServiceItems.builder()
                .id(3003L)
                .type(ServiceItemsType.DEPARTMENTS)
                .sourceId(99L)
                .service(svc)
                .createdBy("tester")
                .isActive(true)
                .build();

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(item);
        }

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ServiceItems deserialized;
        try (ObjectInputStream in = new ObjectInputStream(bis)) {
            deserialized = (ServiceItems) in.readObject();
        }

        assertThat(deserialized.getId()).isEqualTo(3003L);
        assertThat(deserialized.getType()).isEqualTo(ServiceItemsType.DEPARTMENTS);
        assertThat(deserialized.getSourceId()).isEqualTo(99L);
        assertThat(deserialized.getService()).isNotNull();
        assertThat(deserialized.getService().getId()).isEqualTo(33L);
    }
}
