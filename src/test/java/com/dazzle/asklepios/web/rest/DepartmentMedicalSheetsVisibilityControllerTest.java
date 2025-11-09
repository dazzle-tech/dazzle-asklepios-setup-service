package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsVisibility;
import com.dazzle.asklepios.domain.enumeration.MedicalSheets;
import com.dazzle.asklepios.service.DepartmentMedicalSheetsVisibilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DepartmentMedicalSheetsVisibilityControllerTest {

    private DepartmentMedicalSheetsVisibilityService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        service = mock(DepartmentMedicalSheetsVisibilityService.class);
        DepartmentMedicalSheetsVisibilityController controller =
                new DepartmentMedicalSheetsVisibilityController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testCreate() throws Exception {
        DepartmentMedicalSheetsVisibility entity = new DepartmentMedicalSheetsVisibility();
        entity.setId(10L);
        entity.setDepartmentId(1L);
        entity.setMedicalSheet(MedicalSheets.ALLERGIES);

        when(service.create(any())).thenReturn(entity);

        mockMvc.perform(post("/api/setup/department-medical-sheets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"departmentId":1,"medicalSheet":"ALLERGIES"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.departmentId").value(1))
                .andExpect(jsonPath("$.medicalSheet").value("ALLERGIES"));

        verify(service).create(any());
    }

    @Test
    void testGetAll() throws Exception {
        var list = List.of(
                new DepartmentMedicalSheetsVisibility(1L, 1L, MedicalSheets.ALLERGIES),
                new DepartmentMedicalSheetsVisibility(2L, 2L, MedicalSheets.CARDIOLOGY)
        );
        when(service.findAll()).thenReturn(list);

        mockMvc.perform(get("/api/setup/department-medical-sheets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].medicalSheet").value("ALLERGIES"))
                .andExpect(jsonPath("$[1].medicalSheet").value("CARDIOLOGY"));

        verify(service).findAll();
    }

    @Test
    void testGetByDepartment() throws Exception {
        var list = List.of(new DepartmentMedicalSheetsVisibility(1L, 5L, MedicalSheets.ALLERGIES));
        when(service.findByDepartmentId(5L)).thenReturn(list);

        mockMvc.perform(get("/api/setup/department-medical-sheets/department/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].departmentId").value(5))
                .andExpect(jsonPath("$[0].medicalSheet").value("ALLERGIES"));

        verify(service).findByDepartmentId(5L);
    }

    @Test
    void testDelete() throws Exception {
        doNothing().when(service).delete(10L);

        mockMvc.perform(delete("/api/setup/department-medical-sheets/10"))
                .andExpect(status().isNoContent());

        verify(service).delete(10L);
    }

    @Test
    void testBulkSave() throws Exception {
        var list = List.of(
                new DepartmentMedicalSheetsVisibility(11L, 1L, MedicalSheets.ALLERGIES),
                new DepartmentMedicalSheetsVisibility(12L, 1L, MedicalSheets.CARDIOLOGY)
        );
        when(service.bulkSave(ArgumentMatchers.anyList())).thenReturn(list);

        mockMvc.perform(post("/api/setup/department-medical-sheets/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {"departmentId":1,"medicalSheet":"ALLERGIES"},
                                  {"departmentId":1,"medicalSheet":"CARDIOLOGY"}
                                ]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].medicalSheet").value("ALLERGIES"))
                .andExpect(jsonPath("$[1].medicalSheet").value("CARDIOLOGY"));

        verify(service).bulkSave(anyList());
    }
}
