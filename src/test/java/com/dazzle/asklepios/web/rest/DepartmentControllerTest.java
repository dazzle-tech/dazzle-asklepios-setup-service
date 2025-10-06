package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.config.TestSecurityConfig;
import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.service.DepartmentService;
import com.dazzle.asklepios.web.rest.vm.DepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.DepartmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.DepartmentUpdateVM;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;



import java.util.List;
import java.util.Optional;

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

    // GET /api/setup/department
    @Test
    void testGetAllDepartments() throws Exception {
        Department dept = new Department();
        dept.setId(5000L);
        dept.setName("INPATIENT Department");

        when(departmentService.findAll())
                .thenReturn(List.of(DepartmentResponseVM.ofEntity(dept)));

        mockMvc.perform(get("/api/setup/department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(1))
                .andExpect(jsonPath("$.data[0].name").value("INPATIENT Department"));
    }

    // GET /api/setup/department/{id}
    @Test
    void testGetDepartmentById() throws Exception {
        Department dept = new Department();
        dept.setId(5000L);
        dept.setName("INPATIENT Department");

        when(departmentService.findOne(5000L))
                .thenReturn(Optional.of(dept));

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

    // POST /api/setup/department
    @Test
    void testCreateDepartment() throws Exception {
        Department dept = new Department();
        dept.setId(5001L);
        dept.setName("Neurology");

        DepartmentCreateVM createVM = new DepartmentCreateVM(
                "Neurology",
                1L,
                DepartmentType.INPATIENT_WARD,
                true,
                "NEU01",
                "123456789",
                "neuro@hospital.com",
                EncounterType.INPATIENT,
                true,
                "tester"
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

    // PUT /api/setup/department/{id}
    @Test
    void testUpdateDepartment() throws Exception {
        Department dept = new Department();
        dept.setId(5000L);
        dept.setName("Oncology");

        DepartmentUpdateVM updateVM = new DepartmentUpdateVM(
                5000L,
                "Oncology",
                1L,
                DepartmentType.OUTPATIENT_CLINIC,
                true,
                "ONC01",
                "987654321",
                "oncology@hospital.com",
                EncounterType.CLINIC,
                true,
                "admin"
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
                                  "isActive": true,
                                  "lastModifiedBy": "admin"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Oncology"));
    }

    @Test
    void testUpdateDepartment_NotFound() throws Exception {
        DepartmentUpdateVM updateVM = new DepartmentUpdateVM(
                9999L,
                "Unknown",
                1L,
                DepartmentType.OUTPATIENT_CLINIC,
                true,
                "UNK01",
                "000",
                "unknown@hospital.com",
                EncounterType.CLINIC,
                true,
                "admin"
        );

        when(departmentService.update(9999L, updateVM)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/setup/department/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 9999,
                                  "name": "Unknown",
                                  "facilityId": 1,
                                  "departmentType": "OUTPATIENT_CLINIC",
                                  "appointable": true,
                                  "departmentCode": "UNK01",
                                  "phoneNumber": "000",
                                  "email": "unknown@hospital.com",
                                  "encounterType": "CLINIC",
                                  "isActive": true,
                                  "lastModifiedBy": "admin"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    // GET /api/setup/department/facility/{facilityId}
    @Test
    void testGetDepartmentByFacility() throws Exception {
        Department dept = new Department();
        dept.setId(5000L);
        dept.setName("Oncology");

        when(departmentService.findByFacilityId(1L))
                .thenReturn(List.of(dept));

        mockMvc.perform(get("/api/setup/department/facility/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Oncology"));
    }

    // GET /api/setup/department/department-list-by-type
    @Test
    void testGetDepartmentByType() throws Exception {
        Department dept = new Department();
        dept.setId(5000L);
        dept.setName("INPATIENT Department");

        when(departmentService.findByDepartmentType(DepartmentType.INPATIENT_WARD))
                .thenReturn(List.of(dept));

        mockMvc.perform(get("/api/setup/department/department-list-by-type")
                        .header("type", DepartmentType.INPATIENT_WARD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(1))
                .andExpect(jsonPath("$.data[0].name").value("INPATIENT Department"));
    }

    // GET /api/setup/department/department-list-by-name
    @Test
    void testGetDepartmentByName() throws Exception {
        Department dept = new Department();
        dept.setId(5000L);
        dept.setName("INPATIENT Department");

        when(departmentService.findByDepartmentName("INPATIENT Department"))
                .thenReturn(List.of(dept));

        mockMvc.perform(get("/api/setup/department/department-list-by-name")
                        .header("name", "INPATIENT Department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(1))
                .andExpect(jsonPath("$.data[0].name").value("INPATIENT Department"));

    }

    // PATCH /api/setup/department/{id}/toggle-active
    @Test
    void testToggleDepartmentActiveStatus() throws Exception {
        Department dept = new Department();
        dept.setId(5000L);
        dept.setName("Oncology");
        dept.setIsActive(true);

        when(departmentService.toggleIsActive(5000L))
                .thenReturn(Optional.of(dept));

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

    // GET /api/setup/department/department-type
    @Test
    void testGetAllDepartmentTypes() throws Exception {
        mockMvc.perform(get("/api/setup/department/department-type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(DepartmentType.INPATIENT_WARD.name()))
                .andExpect(jsonPath("$[1]").value(DepartmentType.OUTPATIENT_CLINIC.name()));
    }

    // GET /api/setup/department/encounter-type
    @Test
    void testGetAllEncounterTypes() throws Exception {
        mockMvc.perform(get("/api/setup/department/encounter-type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(EncounterType.EMERGENCY.name()))
                .andExpect(jsonPath("$[1]").value(EncounterType.CLINIC.name()));
    }
}
