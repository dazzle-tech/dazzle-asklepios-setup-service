package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.FacilityRepository;

import com.dazzle.asklepios.web.rest.vm.department.DepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.department.DepartmentUpdateVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
                .type(DepartmentType.OUTPATIENT_CLINIC)
                .code("CARD01")
                .isActive(true)
                .build();
    }

    @Test
    void testCreateDepartment_Success() {
        var vm = new DepartmentCreateVM(
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
        var vm = new DepartmentCreateVM(
                "Cardiology", 99L, DepartmentType.OUTPATIENT_CLINIC,
                true, "CARD01", "123456", "email@test.com", null, true, "tester"
        );

        when(facilityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> departmentService.create(vm));
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void testUpdateDepartment_Success() {
        var vm = new DepartmentUpdateVM(
                5000L, "Updated Name", facility.getId(), DepartmentType.OUTPATIENT_CLINIC,
                true, "NEW01", "111", "new@test.com", null, false
        );

        when(facilityRepository.findById(facility.getId())).thenReturn(Optional.of(facility));
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
        when(departmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Department> updated = departmentService.update(department.getId(), vm);

        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Updated Name");
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
    void testFindAll_Paginated_MapsToVM() {
        var pageable = PageRequest.of(0, 10);
        Page<Department> page = new PageImpl<>(List.of(department), pageable, 1);

        when(departmentRepository.findAll(pageable)).thenReturn(page);

        Page<Department> result = departmentService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Cardiology");
        assertThat(result.getContent().get(0).getCode()).isEqualTo("CARD01");
        verify(departmentRepository).findAll(pageable);
    }

    @Test
    void testFindByFacilityId_Paginated() {
        var pageable = PageRequest.of(0, 5);
        Page<Department> page = new PageImpl<>(List.of(department), pageable, 1);

        when(departmentRepository.findByFacilityId(facility.getId(), pageable)).thenReturn(page);

        Page<Department> result = departmentService.findByFacilityId(facility.getId(), pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Cardiology");
        verify(departmentRepository).findByFacilityId(facility.getId(), pageable);
    }

    @Test
    void testFindByDepartmentType_Paginated() {
        var pageable = PageRequest.of(0, 5);
        Page<Department> page = new PageImpl<>(List.of(department), pageable, 1);

        when(departmentRepository.findByType(DepartmentType.OUTPATIENT_CLINIC, pageable))
                .thenReturn(page);

        Page<Department> result = departmentService.findByDepartmentType(DepartmentType.OUTPATIENT_CLINIC, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Cardiology");
        verify(departmentRepository).findByType(DepartmentType.OUTPATIENT_CLINIC, pageable);
    }

    @Test
    void testFindByDepartmentName_Paginated() {
        var pageable = PageRequest.of(0, 5);
        Page<Department> page = new PageImpl<>(List.of(department), pageable, 1);

        when(departmentRepository.findByNameContainingIgnoreCase("Card", pageable))
                .thenReturn(page);

        Page<Department> result = departmentService.findByDepartmentName("Card", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Cardiology");
        verify(departmentRepository).findByNameContainingIgnoreCase("Card", pageable);
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
}
