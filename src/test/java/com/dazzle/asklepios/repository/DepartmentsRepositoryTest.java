package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DepartmentsRepositoryTest {

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private FacilityRepository facilityRepository; // you'll need this repository

    private Facility facility1;
    private Facility facility2;
    private Department cardiology;
    private Department neurology;

    @BeforeEach
    void setUp() {
        departmentsRepository.deleteAll();
        // Create and save facilities
        facility1 = Facility.builder()
                .name("Facility One")
                .isActive(true)
                .code("test662")
                .build();
        facility2 = Facility.builder()
                .name("Facility Two")
                .isActive(true)
                .code("test662soks")
                .build();
        facilityRepository.save(facility1);
        facilityRepository.save(facility2);

        // Create departments
        cardiology = Department.builder()
                .name("Cardiology")
                .departmentType(DepartmentType.OUTPATIENT_CLINIC)
                .facility(facility1)
                .createdBy("tester")
                .departmentCode("CARD01")
                .build();

        neurology = Department.builder()
                .name("Neurology")
                .departmentType(DepartmentType.OUTPATIENT_CLINIC)
                .facility(facility2)
                .createdBy("tester")
                .departmentCode("NEUR01")
                .build();

        departmentsRepository.save(cardiology);
        departmentsRepository.save(neurology);
    }

    @Test
    void testFindByFacilityId() {
        List<Department> result = departmentsRepository.findByFacilityId(facility1.getId());
        assertThat(result).hasSize(1).containsExactly(cardiology);
    }

    @Test
    void testFindByDepartmentType() {
        List<Department> result = departmentsRepository.findByDepartmentType(DepartmentType.OUTPATIENT_CLINIC);
        assertThat(result).hasSize(2).contains(cardiology, neurology);
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        List<Department> result = departmentsRepository.findByNameContainingIgnoreCase("card");
        assertThat(result).hasSize(1).containsExactly(cardiology);

        result = departmentsRepository.findByNameContainingIgnoreCase("NEURO");
        assertThat(result).hasSize(1).containsExactly(neurology);
    }


}
