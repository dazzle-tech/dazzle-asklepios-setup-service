package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.AgeUnit;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
                .type(DepartmentType.INPATIENT_WARD)
                .appointable(true)
                .code("CARD01")
                .phoneNumber("123456789")
                .email("cardio@hospital.com")
                .encounterType(EncounterType.INPATIENT)
                .isActive(true)
                .hasMedicalSheets(true)
                .hasNurseMedicalSheets(true)
                .ageSpecific(true)
                .fromAge(1)
                .fromAgeUnit(AgeUnit.DAYS)
                .toAge(12)
                .toAgeUnit(AgeUnit.YEARS)
                .build();

        assertThat(dept.getId()).isEqualTo(5004L);
        assertThat(dept.getName()).isEqualTo("Cardiology");
        assertThat(dept.getType()).isEqualTo(DepartmentType.INPATIENT_WARD);
        assertThat(dept.getEncounterType()).isEqualTo(EncounterType.INPATIENT);
        assertThat(dept.getIsActive()).isTrue();
        assertThat(dept.getFacility().getId()).isEqualTo(1L);
        assertThat(dept.getAgeSpecific()).isTrue();
        assertThat(dept.getFromAge()).isEqualTo(1);
        assertThat(dept.getFromAgeUnit()).isEqualTo(AgeUnit.DAYS);
        assertThat(dept.getToAge()).isEqualTo(12);
        assertThat(dept.getToAgeUnit()).isEqualTo(AgeUnit.YEARS);
    }

    @Test
    void testEqualsAndHashCode() {
        Facility facility = new Facility();
        facility.setId(5003L);

        Department dept1 = Department.builder()
                .id(5003L)
                .facility(facility)
                .name("Cardiology")
                .type(DepartmentType.OUTPATIENT_CLINIC)
                .code("CARD01")
                .build();

        Department dept2 = Department.builder()
                .id(5003L) // same id → should be equal
                .facility(facility)
                .name("Cardiology")
                .type(DepartmentType.OUTPATIENT_CLINIC)
                .code("CARD01")
                .build();

        assertThat(dept1).isEqualTo(dept2);
        assertThat(dept1.hashCode()).isEqualTo(dept2.hashCode());
    }

    @Test
    void testSerialization() throws Exception {
        Department dept = Department.builder()
                .id(5005L)
                .name("Radiology")
                .type(DepartmentType.OUTPATIENT_CLINIC)
                .code("RAD01")
                .ageSpecific(true)
                .fromAge(3)
                .fromAgeUnit(AgeUnit.MONTHS)
                .toAge(5)
                .toAgeUnit(AgeUnit.YEARS)
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
        assertThat(deserialized.getCode()).isEqualTo("RAD01");
        assertThat(deserialized.getAgeSpecific()).isTrue();
        assertThat(deserialized.getFromAge()).isEqualTo(3);
        assertThat(deserialized.getFromAgeUnit()).isEqualTo(AgeUnit.MONTHS);
        assertThat(deserialized.getToAge()).isEqualTo(5);
        assertThat(deserialized.getToAgeUnit()).isEqualTo(AgeUnit.YEARS);
    }

}
