package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.MedicalSheets;
import com.dazzle.asklepios.repository.DepartmentMedicalSheetsNurseVisibilityRepository;
import com.dazzle.asklepios.service.DepartmentMedicalSheetsNurseVisibilityService;
import com.dazzle.asklepios.web.rest.vm.DepartmentMedicalSheetsNurseVisibilityVM;
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

class DepartmentMedicalSheetsNurseVisibilityControllerTest {

    private DepartmentMedicalSheetsNurseVisibilityService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        service = mock(DepartmentMedicalSheetsNurseVisibilityService.class);
        DepartmentMedicalSheetsNurseVisibilityController controller =
                new DepartmentMedicalSheetsNurseVisibilityController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testCreate() throws Exception {
        DepartmentMedicalSheetsNurseVisibilityVM vm =
                new DepartmentMedicalSheetsNurseVisibilityVM(1L, MedicalSheets.ALLERGIES);
        when(service.create(any())).thenReturn(vm);

        mockMvc.perform(post("/api/setup/department-medical-sheets-nurse")
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
                new DepartmentMedicalSheetsNurseVisibilityVM(1L, MedicalSheets.ALLERGIES),
                new DepartmentMedicalSheetsNurseVisibilityVM(2L, MedicalSheets.CARDIOLOGY)
        );
        when(service.findAll()).thenReturn(list);

        mockMvc.perform(get("/api/setup/department-medical-sheets-nurse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].medicalSheet").value("ALLERGIES"));

        verify(service).findAll();
    }

    @Test
    void testGetByDepartment() throws Exception {
        var list = List.of(new DepartmentMedicalSheetsNurseVisibilityVM(5L, MedicalSheets.ALLERGIES));
        when(service.findByDepartmentId(5L)).thenReturn(list);

        mockMvc.perform(get("/api/setup/department-medical-sheets-nurse/department/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].departmentId").value(5))
                .andExpect(jsonPath("$[0].medicalSheet").value("ALLERGIES"));

        verify(service).findByDepartmentId(5L);
    }

    @Test
    void testDelete() throws Exception {
        doNothing().when(service).delete(10L);

        mockMvc.perform(delete("/api/setup/department-medical-sheets-nurse/10"))
                .andExpect(status().isNoContent());

        verify(service).delete(10L);
    }

    @Test
    void testBulkSave() throws Exception {
        var inputList = List.of(
                new DepartmentMedicalSheetsNurseVisibilityVM(1L, MedicalSheets.ALLERGIES),
                new DepartmentMedicalSheetsNurseVisibilityVM(1L, MedicalSheets.CARDIOLOGY)
        );
        when(service.bulkSave(ArgumentMatchers.anyList())).thenReturn(inputList);

        mockMvc.perform(post("/api/setup/department-medical-sheets-nurse/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {"departmentId":1,"medicalSheet":"ALLERGIES"},
                                  {"departmentId":1,"medicalSheet":"CARDIOLOGY"}
                                ]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].medicalSheet").value("CARDIOLOGY"));

        verify(service).bulkSave(anyList());
    }
}
