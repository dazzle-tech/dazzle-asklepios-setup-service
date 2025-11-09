package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsVisibility;
import com.dazzle.asklepios.domain.enumeration.MedicalSheets;
import com.dazzle.asklepios.repository.DepartmentMedicalSheetsVisibilityRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        DepartmentMedicalSheetsVisibility entity = new DepartmentMedicalSheetsVisibility();
        entity.setDepartmentId(1L);
        entity.setMedicalSheet(MedicalSheets.CARE_PLAN_AND_GOALS);

        DepartmentMedicalSheetsVisibility savedEntity = new DepartmentMedicalSheetsVisibility();
        savedEntity.setId(10L);
        savedEntity.setDepartmentId(1L);
        savedEntity.setMedicalSheet(MedicalSheets.CARE_PLAN_AND_GOALS);

        when(repository.save(any())).thenReturn(savedEntity);

        DepartmentMedicalSheetsVisibility result = service.create(entity);

        assertThat(result.getDepartmentId()).isEqualTo(1L);
        assertThat(result.getMedicalSheet()).isEqualTo(MedicalSheets.CARE_PLAN_AND_GOALS);
        verify(repository, times(1)).save(any(DepartmentMedicalSheetsVisibility.class));
    }

    @Test
    void testFindAll() {
        DepartmentMedicalSheetsVisibility e1 = new DepartmentMedicalSheetsVisibility(1L, 1L, MedicalSheets.CARE_PLAN_AND_GOALS);
        DepartmentMedicalSheetsVisibility e2 = new DepartmentMedicalSheetsVisibility(2L, 2L, MedicalSheets.MEDICAL_WARNINGS);
        when(repository.findAll()).thenReturn(List.of(e1, e2));

        var result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMedicalSheet()).isEqualTo(MedicalSheets.CARE_PLAN_AND_GOALS);
        verify(repository).findAll();
    }

    @Test
    void testFindByDepartmentId() {
        DepartmentMedicalSheetsVisibility e = new DepartmentMedicalSheetsVisibility(1L, 5L, MedicalSheets.CARDIOLOGY);
        when(repository.findByDepartmentId(5L)).thenReturn(List.of(e));

        var result = service.findByDepartmentId(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDepartmentId()).isEqualTo(5L);
        verify(repository).findByDepartmentId(5L);
    }

    @Test
    void testDelete() {
        service.delete(10L);
        verify(repository).deleteById(10L);
    }

    @Test
    void testBulkSave() {
        DepartmentMedicalSheetsVisibility e1 = new DepartmentMedicalSheetsVisibility(11L, 1L, MedicalSheets.ALLERGIES);
        DepartmentMedicalSheetsVisibility e2 = new DepartmentMedicalSheetsVisibility(12L, 1L, MedicalSheets.CARDIOLOGY);
        List<DepartmentMedicalSheetsVisibility> list = List.of(e1, e2);

        when(repository.saveAll(any())).thenReturn(list);

        var result = service.bulkSave(list);

        verify(repository).deleteByDepartmentId(1L);
        verify(repository).saveAll(any());
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMedicalSheet()).isEqualTo(MedicalSheets.ALLERGIES);
        assertThat(result.get(1).getMedicalSheet()).isEqualTo(MedicalSheets.CARDIOLOGY);
    }

    @Test
    void testBulkSaveEmptyListThrowsBadRequest() {
        assertThatThrownBy(() -> service.bulkSave(List.of()))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("Bulk save list cannot be empty");
        verify(repository, never()).saveAll(any());
        verify(repository, never()).deleteByDepartmentId(any());
    }
}
