package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.config.TestSecurityConfig;
import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.service.DepartmentService;

import com.dazzle.asklepios.web.rest.vm.department.DepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.department.DepartmentUpdateVM;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DepartmentController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService departmentService;

    @Test
    void testGetAllDepartments_Paginated() throws Exception {
        Department dept = new Department();
        dept.setId(5000L);
        dept.setName("INPATIENT Department");

        var pageable = PageRequest.of(0, 10);
        Page<Department> page =
                new PageImpl<>(List.of(dept), pageable, 1);

        when(departmentService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/setup/department")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].name").value("INPATIENT Department"));
    }

    @Test
    void testGetDepartmentById() throws Exception {
        Department dept = new Department();
        dept.setId(5000L);
        dept.setName("INPATIENT Department");

        when(departmentService.findOne(5000L)).thenReturn(Optional.of(dept));

        mockMvc.perform(get("/api/setup/department/5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("INPATIENT Department"));
    }

    @Test
    void testGetDepartmentById_NotFound() throws Exception {
        when(departmentService.findOne(9999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/setup/department/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateDepartment() throws Exception {
        Department dept = new Department();
        dept.setId(5001L);
        dept.setName("Neurology");

        DepartmentCreateVM createVM = new DepartmentCreateVM(
                "Neurology", 1L, DepartmentType.INPATIENT_WARD, true, "NEU01",
                "123456789", "neuro@hospital.com", EncounterType.INPATIENT, true, "tester"
        );

        when(departmentService.create(createVM)).thenReturn(dept);

        mockMvc.perform(post("/api/setup/department")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "name": "Neurology",
                      "facilityId": 1,
                      "departmentType": "INPATIENT_WARD",
                      "appointable": true,
                      "departmentCode": "NEU01",
                      "phoneNumber": "123456789",
                      "email": "neuro@hospital.com",
                      "encounterType": "INPATIENT",
                      "isActive": true,
                      "createdBy": "tester"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/setup/api/department/5001"))
                .andExpect(jsonPath("$.name").value("Neurology"));
    }

    @Test
    void testUpdateDepartment() throws Exception {
        Department dept = new Department();
        dept.setId(5000L);
        dept.setName("Oncology");

        DepartmentUpdateVM updateVM = new DepartmentUpdateVM(
                5000L, "Oncology", 1L, DepartmentType.OUTPATIENT_CLINIC, true,
                "ONC01", "987654321", "oncology@hospital.com", EncounterType.CLINIC, true
        );

        when(departmentService.update(5000L, updateVM)).thenReturn(Optional.of(dept));

        mockMvc.perform(put("/api/setup/department/5000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "id": 5000,
                      "name": "Oncology",
                      "facilityId": 1,
                      "departmentType": "OUTPATIENT_CLINIC",
                      "appointable": true,
                      "departmentCode": "ONC01",
                      "phoneNumber": "987654321",
                      "email": "oncology@hospital.com",
                      "encounterType": "CLINIC",
                      "isActive": true
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Oncology"));
    }

    @Test
    void testGetDepartmentByFacility_Paginated() throws Exception {
        Department dept = new Department();
        dept.setId(5000L);
        dept.setName("Oncology");

        var pageable = PageRequest.of(0, 10);
        Page<Department> page =
                new PageImpl<>(List.of(dept), pageable, 1);

        when(departmentService.findByFacilityId(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/setup/department/by-facility/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].name").value("Oncology"));
    }

    @Test
    void testGetDepartmentByType_Paginated() throws Exception {
        Department dept = new Department();
        dept.setId(5000L);
        dept.setName("INPATIENT Department");

        var pageable = PageRequest.of(0, 10);
        Page<Department> page =
                new PageImpl<>(List.of(dept), pageable, 1);

        when(departmentService.findByDepartmentType(eq(DepartmentType.INPATIENT_WARD), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/setup/department/by-type/INPATIENT_WARD")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].name").value("INPATIENT Department"));
    }

    @Test
    void testGetDepartmentByName_Paginated() throws Exception {
        Department dept = new Department();
        dept.setId(5000L);
        dept.setName("INPATIENT Department");

        var pageable = PageRequest.of(0, 10);
        Page<Department> page =
                new PageImpl<>(List.of(dept), pageable, 1);

        when(departmentService.findByDepartmentName(eq("INPATIENT Department"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/setup/department/by-name/INPATIENT Department")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].name").value("INPATIENT Department"));
    }

    @Test
    void testToggleDepartmentActiveStatus() throws Exception {
        Department dept = new Department();
        dept.setId(5000L);
        dept.setName("Oncology");
        dept.setIsActive(true);

        when(departmentService.toggleIsActive(5000L)).thenReturn(Optional.of(dept));

        mockMvc.perform(patch("/api/setup/department/5000/toggle-active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Oncology"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(departmentService, times(1)).toggleIsActive(5000L);
    }

    @Test
    void testToggleDepartmentActiveStatus_NotFound() throws Exception {
        when(departmentService.toggleIsActive(9999L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/setup/department/9999/toggle-active"))
                .andExpect(status().isNotFound());
    }
}