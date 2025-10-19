package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentTest {

    @Test
    void testBuilderAndGetters() {
        Facility facility = new Facility();
        facility.setId(1L);

        Department dept = Department.builder()
                .id(5004L)
                .facility(facility)
                .name("Cardiology")
                .createdBy("tester")
                .createdDate(LocalDateTime.now())
                .departmentType(DepartmentType.INPATIENT_WARD)
                .lastModifiedBy("admin")
                .lastModifiedDate(LocalDateTime.now())
                .appointable(true)
                .departmentCode("CARD01")
                .phoneNumber("123456789")
                .email("cardio@hospital.com")
                .encounterType(EncounterType.INPATIENT)
                .isActive(true)
                .build();

        assertThat(dept.getId()).isEqualTo(5004L);
        assertThat(dept.getName()).isEqualTo("Cardiology");
        assertThat(dept.getDepartmentType()).isEqualTo(DepartmentType.INPATIENT_WARD);
        assertThat(dept.getEncounterType()).isEqualTo(EncounterType.INPATIENT);
        assertThat(dept.getIsActive()).isTrue();
        assertThat(dept.getFacility().getId()).isEqualTo(1L);
    }

    @Test
    void testEqualsAndHashCode() {
        Facility facility = new Facility();
        facility.setId(5003L);

        Department dept1 = Department.builder()
                .id(5003L)
                .facility(facility)
                .name("Cardiology")
                .createdBy("tester")
                .departmentType(DepartmentType.OUTPATIENT_CLINIC)
                .departmentCode("CARD01")
                .build();

        Department dept2 = Department.builder()
                .id(5003L) // same id → should be equal
                .facility(facility)
                .name("Cardiology")
                .createdBy("tester")
                .departmentType(DepartmentType.OUTPATIENT_CLINIC)
                .departmentCode("CARD01")
                .build();

        assertThat(dept1).isEqualTo(dept2);
        assertThat(dept1.hashCode()).isEqualTo(dept2.hashCode());
    }

    @Test
    void testSerialization() throws Exception {
        Department dept = Department.builder()
                .id(5005L)
                .name("Radiology")
                .createdBy("tester")
                .departmentType(DepartmentType.OUTPATIENT_CLINIC)
                .departmentCode("RAD01")
                .build();

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(dept);

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        Department deserialized = (Department) in.readObject();

        assertThat(deserialized.getName()).isEqualTo("Radiology");
        assertThat(deserialized.getDepartmentCode()).isEqualTo("RAD01");
    }

}
