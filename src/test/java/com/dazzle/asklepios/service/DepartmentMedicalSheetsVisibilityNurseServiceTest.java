package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsNurseVisbility;

import com.dazzle.asklepios.domain.enumeration.MedicalSheets;
import com.dazzle.asklepios.repository.DepartmentMedicalSheetsNurseVisibilityRepository;
import com.dazzle.asklepios.web.rest.vm.DepartmentMedicalSheetsNurseVisibilityVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DepartmentMedicalSheetsVisibilityNurseServiceTest {

    private DepartmentMedicalSheetsNurseVisibilityRepository repository;
    private DepartmentMedicalSheetsNurseVisibilityService service;

    @BeforeEach
    void setUp() {
        repository = mock(DepartmentMedicalSheetsNurseVisibilityRepository.class);
        service = new DepartmentMedicalSheetsNurseVisibilityService(repository);
    }

    @Test
    void testCreate() {
        DepartmentMedicalSheetsNurseVisibilityVM vm =
                new DepartmentMedicalSheetsNurseVisibilityVM(1L, MedicalSheets.CARE_PLAN_AND_GOALS);

        DepartmentMedicalSheetsNurseVisbility savedEntity = new DepartmentMedicalSheetsNurseVisbility();
        savedEntity.setId(10L);
        savedEntity.setDepartmentId(1L);
        savedEntity.setMedicalSheet(MedicalSheets.CARE_PLAN_AND_GOALS);

        when(repository.save(any())).thenReturn(savedEntity);

        DepartmentMedicalSheetsNurseVisibilityVM result = service.create(vm);

        assertThat(result.departmentId()).isEqualTo(1L);
        assertThat(result.medicalSheet()).isEqualTo(MedicalSheets.CARE_PLAN_AND_GOALS);
        verify(repository, times(1)).save(any(DepartmentMedicalSheetsNurseVisbility.class));
    }

    @Test
    void testFindAll() {
        DepartmentMedicalSheetsNurseVisbility e1 = new DepartmentMedicalSheetsNurseVisbility(1L, 1L, MedicalSheets.CARE_PLAN_AND_GOALS);
        DepartmentMedicalSheetsNurseVisbility e2 = new DepartmentMedicalSheetsNurseVisbility(2L, 2L, MedicalSheets.MEDICAL_WARNINGS);
        when(repository.findAll()).thenReturn(List.of(e1, e2));

        var result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).medicalSheet()).isEqualTo(MedicalSheets.CARE_PLAN_AND_GOALS);
        verify(repository).findAll();
    }

    @Test
    void testFindByDepartmentId() {
        DepartmentMedicalSheetsNurseVisbility e = new DepartmentMedicalSheetsNurseVisbility(1L, 5L, MedicalSheets.CARDIOLOGY);
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
        var vm1 = new DepartmentMedicalSheetsNurseVisibilityVM(1L, MedicalSheets.ALLERGIES);
        var vm2 = new DepartmentMedicalSheetsNurseVisibilityVM(1L, MedicalSheets.CARDIOLOGY);
        List<DepartmentMedicalSheetsNurseVisibilityVM> list = List.of(vm1, vm2);

        DepartmentMedicalSheetsNurseVisbility e1 = new DepartmentMedicalSheetsNurseVisbility(11L, 1L, MedicalSheets.ALLERGIES);
        DepartmentMedicalSheetsNurseVisbility e2 = new DepartmentMedicalSheetsNurseVisbility(12L, 1L, MedicalSheets.CARDIOLOGY);

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
