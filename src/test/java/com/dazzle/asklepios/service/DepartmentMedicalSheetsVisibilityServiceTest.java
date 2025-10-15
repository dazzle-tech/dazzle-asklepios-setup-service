package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsVisibility;
import com.dazzle.asklepios.domain.enumeration.MedicalSheets;
import com.dazzle.asklepios.repository.DepartmentMedicalSheetsVisibilityRepository;
import com.dazzle.asklepios.web.rest.vm.DepartmentMedicalSheetsVisibilityVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DepartmentMedicalSheetsVisibilityServiceTest {

    private DepartmentMedicalSheetsVisibilityRepository repository;
    private DepartmentMedicalSheetsVisibilityService service;

    @BeforeEach
    void setUp() {
        repository = mock(DepartmentMedicalSheetsVisibilityRepository.class);
        service = new DepartmentMedicalSheetsVisibilityService(repository);
    }

    @Test
    void testCreate() {
        DepartmentMedicalSheetsVisibilityVM vm =
                new DepartmentMedicalSheetsVisibilityVM(1L, MedicalSheets.CARE_PLAN_AND_GOALS);

        DepartmentMedicalSheetsVisibility savedEntity = new DepartmentMedicalSheetsVisibility();
        savedEntity.setId(10L);
        savedEntity.setDepartmentId(1L);
        savedEntity.setMedicalSheet(MedicalSheets.CARE_PLAN_AND_GOALS);

        when(repository.save(any())).thenReturn(savedEntity);

        DepartmentMedicalSheetsVisibilityVM result = service.create(vm);

        assertThat(result.departmentId()).isEqualTo(1L);
        assertThat(result.medicalSheet()).isEqualTo(MedicalSheets.CARE_PLAN_AND_GOALS);
        verify(repository, times(1)).save(any(DepartmentMedicalSheetsVisibility.class));
    }

    @Test
    void testFindAll() {
        DepartmentMedicalSheetsVisibility e1 = new DepartmentMedicalSheetsVisibility(1L, 1L, MedicalSheets.CARE_PLAN_AND_GOALS);
        DepartmentMedicalSheetsVisibility e2 = new DepartmentMedicalSheetsVisibility(2L, 2L, MedicalSheets.MEDICAL_WARNINGS);
        when(repository.findAll()).thenReturn(List.of(e1, e2));

        var result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).medicalSheet()).isEqualTo(MedicalSheets.CARE_PLAN_AND_GOALS);
        verify(repository).findAll();
    }

    @Test
    void testFindByDepartmentId() {
        DepartmentMedicalSheetsVisibility e = new DepartmentMedicalSheetsVisibility(1L, 5L, MedicalSheets.CARDIOLOGY);
        when(repository.findByDepartmentId(5L)).thenReturn(List.of(e));

        var result = service.findByDepartmentId(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).departmentId()).isEqualTo(5L);
        verify(repository).findByDepartmentId(5L);
    }

    @Test
    void testDelete() {
        service.delete(10L);
        verify(repository).deleteById(10L);
    }

    @Test
    void testBulkSave() {
        var vm1 = new DepartmentMedicalSheetsVisibilityVM(1L, MedicalSheets.ALLERGIES);
        var vm2 = new DepartmentMedicalSheetsVisibilityVM(1L, MedicalSheets.CARDIOLOGY);
        List<DepartmentMedicalSheetsVisibilityVM> list = List.of(vm1, vm2);

        DepartmentMedicalSheetsVisibility e1 = new DepartmentMedicalSheetsVisibility(11L, 1L, MedicalSheets.ALLERGIES);
        DepartmentMedicalSheetsVisibility e2 = new DepartmentMedicalSheetsVisibility(12L, 1L, MedicalSheets.CARDIOLOGY);

        when(repository.saveAll(any())).thenReturn(List.of(e1, e2));

        var result = service.bulkSave(list);

        verify(repository).deleteByDepartmentId(1L);
        verify(repository).saveAll(any());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).medicalSheet()).isEqualTo(MedicalSheets.ALLERGIES);
        assertThat(result.get(1).medicalSheet()).isEqualTo(MedicalSheets.CARDIOLOGY);
    }


    @Test
    void testBulkSaveEmptyList() {
        var result = service.bulkSave(List.of());
        assertThat(result).isEmpty();
        verify(repository, never()).saveAll(any());
        verify(repository, never()).deleteByDepartmentId(any());
    }
}
