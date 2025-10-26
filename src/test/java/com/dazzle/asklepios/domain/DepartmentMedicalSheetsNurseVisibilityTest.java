
package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.MedicalSheets;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentMedicalSheetsNurseVisibilityTest {

    @Test
    void testEntityBuilderAndGetters() {
        DepartmentMedicalSheetsNurseVisbility entity = DepartmentMedicalSheetsNurseVisbility.builder()
                .id(1L)
                .departmentId(10L)
                .medicalSheet(MedicalSheets.ALLERGIES)
                .build();

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getDepartmentId()).isEqualTo(10L);
        assertThat(entity.getMedicalSheet()).isEqualTo(MedicalSheets.ALLERGIES);
    }

    @Test
    void testEqualsAndHashCode() {
        DepartmentMedicalSheetsNurseVisbility a = DepartmentMedicalSheetsNurseVisbility.builder()
                .departmentId(1L)
                .medicalSheet(MedicalSheets.ALLERGIES)
                .build();

        DepartmentMedicalSheetsNurseVisbility b = DepartmentMedicalSheetsNurseVisbility.builder()
                .departmentId(1L)
                .medicalSheet(MedicalSheets.ALLERGIES)
                .build();

        DepartmentMedicalSheetsNurseVisbility c = DepartmentMedicalSheetsNurseVisbility.builder()
                .departmentId(2L)
                .medicalSheet(MedicalSheets.ALLERGIES)
                .build();

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void testSetters() {
        DepartmentMedicalSheetsNurseVisbility entity = new DepartmentMedicalSheetsNurseVisbility();
        entity.setId(5L);
        entity.setDepartmentId(20L);
        entity.setMedicalSheet(MedicalSheets.ALLERGIES);

        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getDepartmentId()).isEqualTo(20L);
        assertThat(entity.getMedicalSheet()).isEqualTo(MedicalSheets.ALLERGIES);
    }

    @Test
    void testSerialization() throws Exception {
        DepartmentMedicalSheetsNurseVisbility original = DepartmentMedicalSheetsNurseVisbility.builder()
                .id(100L)
                .departmentId(200L)
                .medicalSheet(MedicalSheets.ALLERGIES)
                .build();

        // Serialize
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(original);
            bytes = bos.toByteArray();
        }

        // Deserialize
        DepartmentMedicalSheetsNurseVisbility deserialized;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            deserialized = (DepartmentMedicalSheetsNurseVisbility) in.readObject();
        }

        // Verify fields preserved
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getId()).isEqualTo(original.getId());
        assertThat(deserialized.getDepartmentId()).isEqualTo(original.getDepartmentId());
        assertThat(deserialized.getMedicalSheet()).isEqualTo(original.getMedicalSheet());
        assertThat(deserialized).isEqualTo(original);
    }
}
