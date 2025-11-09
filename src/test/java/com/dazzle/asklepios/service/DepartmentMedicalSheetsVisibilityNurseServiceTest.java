package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsNurseVisbility;
import com.dazzle.asklepios.domain.enumeration.MedicalSheets;
import com.dazzle.asklepios.repository.DepartmentMedicalSheetsNurseVisibilityRepository;
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

class DepartmentMedicalSheetsNurseVisibilityServiceTest {

    private DepartmentMedicalSheetsNurseVisibilityRepository repository;
    private DepartmentMedicalSheetsNurseVisibilityService service;

    @BeforeEach
    void setUp() {
        repository = mock(DepartmentMedicalSheetsNurseVisibilityRepository.class);
        service = new DepartmentMedicalSheetsNurseVisibilityService(repository);
    }

    @Test
    void testCreate() {
        DepartmentMedicalSheetsNurseVisbility entity = new DepartmentMedicalSheetsNurseVisbility();
        entity.setDepartmentId(1L);
        entity.setMedicalSheet(MedicalSheets.CARE_PLAN_AND_GOALS);

        DepartmentMedicalSheetsNurseVisbility savedEntity = new DepartmentMedicalSheetsNurseVisbility();
        savedEntity.setId(10L);
        savedEntity.setDepartmentId(1L);
        savedEntity.setMedicalSheet(MedicalSheets.CARE_PLAN_AND_GOALS);

        when(repository.save(any())).thenReturn(savedEntity);

        DepartmentMedicalSheetsNurseVisbility result = service.create(entity);

        assertThat(result.getDepartmentId()).isEqualTo(1L);
        assertThat(result.getMedicalSheet()).isEqualTo(MedicalSheets.CARE_PLAN_AND_GOALS);
        verify(repository, times(1)).save(any(DepartmentMedicalSheetsNurseVisbility.class));
    }

    @Test
    void testCreateInvalidMedicalSheetThrows() {
        DepartmentMedicalSheetsNurseVisbility entity = new DepartmentMedicalSheetsNurseVisbility();
        entity.setDepartmentId(1L);
        entity.setMedicalSheet(null);

        assertThatThrownBy(() -> service.create(entity))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("Invalid medical sheet code");

        verify(repository, never()).save(any());
    }

    @Test
    void testFindAll() {
        DepartmentMedicalSheetsNurseVisbility e1 = new DepartmentMedicalSheetsNurseVisbility(1L, 1L, MedicalSheets.CARE_PLAN_AND_GOALS);
        DepartmentMedicalSheetsNurseVisbility e2 = new DepartmentMedicalSheetsNurseVisbility(2L, 2L, MedicalSheets.MEDICAL_WARNINGS);
        when(repository.findAll()).thenReturn(List.of(e1, e2));

        var result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMedicalSheet()).isEqualTo(MedicalSheets.CARE_PLAN_AND_GOALS);
        verify(repository).findAll();
    }

    @Test
    void testFindByDepartmentId() {
        DepartmentMedicalSheetsNurseVisbility e = new DepartmentMedicalSheetsNurseVisbility(1L, 5L, MedicalSheets.CARDIOLOGY);
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
        DepartmentMedicalSheetsNurseVisbility e1 = new DepartmentMedicalSheetsNurseVisbility(11L, 1L, MedicalSheets.ALLERGIES);
        DepartmentMedicalSheetsNurseVisbility e2 = new DepartmentMedicalSheetsNurseVisbility(12L, 1L, MedicalSheets.CARDIOLOGY);
        List<DepartmentMedicalSheetsNurseVisbility> list = List.of(e1, e2);

        when(repository.saveAll(any())).thenReturn(List.of(e1, e2));

        var result = service.bulkSave(list);

        verify(repository).deleteByDepartmentId(1L);
        verify(repository).saveAll(any());
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMedicalSheet()).isEqualTo(MedicalSheets.ALLERGIES);
    }

    @Test
    void testBulkSaveEmptyListThrows() {
        assertThatThrownBy(() -> service.bulkSave(List.of()))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("Bulk save list cannot be empty");

        verify(repository, never()).saveAll(any());
        verify(repository, never()).deleteByDepartmentId(any());
    }

    @Test
    void testBulkSaveMissingDepartmentThrows() {
        DepartmentMedicalSheetsNurseVisbility e = new DepartmentMedicalSheetsNurseVisbility();
        e.setDepartmentId(null);
        e.setMedicalSheet(MedicalSheets.ALLERGIES);

        assertThatThrownBy(() -> service.bulkSave(List.of(e)))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("Missing department id");

        verify(repository, never()).saveAll(any());
    }

    @Test
    void testBulkSaveInvalidMedicalSheetThrows() {
        DepartmentMedicalSheetsNurseVisbility e = new DepartmentMedicalSheetsNurseVisbility();
        e.setDepartmentId(1L);
        e.setMedicalSheet(null);

        assertThatThrownBy(() -> service.bulkSave(List.of(e)))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("Invalid medical sheet code");

        verify(repository, never()).saveAll(any());
    }
}
