package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.web.rest.vm.DepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.DepartmentUpdateVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DepartmentServiceTest {

    @Mock
    private DepartmentsRepository departmentRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Facility facility;
    private Department department;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        facility = Facility.builder()
                .id(1L)
                .name("Facility One")
                .isActive(true)
                .type(FacilityType.HOSPITAL)
                .code("76438")
                .build();

        department = Department.builder()
                .id(1L)
                .name("Cardiology")
                .facility(facility)
                .departmentType(DepartmentType.OUTPATIENT_CLINIC)
                .createdBy("tester")
                .departmentCode("CARD01")
                .isActive(true)
                .build();
    }

    @Test
    void testCreateDepartment_Success() {
        DepartmentCreateVM vm = new DepartmentCreateVM(
                "Cardiology", facility.getId(), DepartmentType.OUTPATIENT_CLINIC,
                true, "CARD01", "123456", "email@test.com", null, true, "tester"
        );

        when(facilityRepository.findById(facility.getId())).thenReturn(Optional.of(facility));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        Department result = departmentService.create(vm);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Cardiology");
        assertThat(result.getFacility()).isEqualTo(facility);

        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void testCreateDepartment_FacilityNotFound() {
        DepartmentCreateVM vm = new DepartmentCreateVM(
                "Cardiology", 99L, DepartmentType.OUTPATIENT_CLINIC,
                true, "CARD01", "123456", "email@test.com", null, true, "tester"
        );

        when(facilityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> departmentService.create(vm));

        verify(departmentRepository, never()).save(any());
    }

    @Test
    void testUpdateDepartment_Success() {
        DepartmentUpdateVM vm = new DepartmentUpdateVM( 5000L,
                "Updated Name", facility.getId(), DepartmentType.OUTPATIENT_CLINIC,
                true, "NEW01", "111", "new@test.com", null, false, "modifier"
        );

        when(facilityRepository.findById(facility.getId())).thenReturn(Optional.of(facility));
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
        when(departmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Department> updated = departmentService.update(department.getId(), vm);

        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Updated Name");
        assertThat(updated.get().getLastModifiedBy()).isEqualTo("modifier");
    }

    @Test
    void testToggleIsActive() {
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
        when(departmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Department> toggled = departmentService.toggleIsActive(department.getId());

        assertThat(toggled).isPresent();
        assertThat(toggled.get().getIsActive()).isFalse();
    }

    @Test
    void testFindByFacilityId() {
        when(departmentRepository.findByFacilityId(facility.getId()))
                .thenReturn(List.of(department));

        List<Department> result = departmentService.findByFacilityId(facility.getId());

        assertThat(result).hasSize(1).contains(department);
    }

    @Test
    void testFindAll() {
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        var result = departmentService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo(department.getName());
        assertThat(result.get(0).departmentCode()).isEqualTo(department.getDepartmentCode());

        verify(departmentRepository).findAll();
    }

    @Test
    void testFindOne_Found() {
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));

        Optional<Department> result = departmentService.findOne(department.getId());

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(department);

        verify(departmentRepository).findById(department.getId());
    }

    @Test
    void testFindOne_NotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Department> result = departmentService.findOne(99L);

        assertThat(result).isEmpty();

        verify(departmentRepository).findById(99L);
    }

    @Test
    void testFindByDepartmentType() {
        when(departmentRepository.findByDepartmentType(DepartmentType.OUTPATIENT_CLINIC))
                .thenReturn(List.of(department));

        List<Department> result = departmentService.findByDepartmentType(DepartmentType.OUTPATIENT_CLINIC);

        assertThat(result).hasSize(1).contains(department);
        verify(departmentRepository).findByDepartmentType(DepartmentType.OUTPATIENT_CLINIC);
    }

    @Test
    void testFindByDepartmentName() {
        when(departmentRepository.findByNameContainingIgnoreCase("Card"))
                .thenReturn(List.of(department));

        List<Department> result = departmentService.findByDepartmentName("Card");

        assertThat(result).hasSize(1).contains(department);
        verify(departmentRepository).findByNameContainingIgnoreCase("Card");
    }

}
