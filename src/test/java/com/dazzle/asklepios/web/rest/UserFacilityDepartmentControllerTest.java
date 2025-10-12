package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.config.TestSecurityConfig;
import com.dazzle.asklepios.service.UserFacilityDepartmentService;
import com.dazzle.asklepios.web.rest.vm.UserFacilityDepartmentResponseVM;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserFacilityDepartmentController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class UserFacilityDepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserFacilityDepartmentService service;

    @Test
    void testCreateUserFacilityDepartment() throws Exception {
        var resp = new UserFacilityDepartmentResponseVM(23L, 5L, 2L, 10L, true);
        when(service.createUserFacilityDepartment(org.mockito.ArgumentMatchers.any()))
                .thenReturn(resp);

        mockMvc.perform(post("/api/setup/user-facility-departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "userId": 5,
                              "departmentId": 10,
                              "isActive": true,
                              "createdBy": "tester",
                              "createdDate": "2024-01-01T00:00:00Z"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/user-facility-departments/23")))
                .andExpect(jsonPath("$.id").value(23))
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.departmentId").value(10))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void testToggleActiveStatus() throws Exception {
        mockMvc.perform(patch("/api/setup/user-facility-departments/{id}/toggle", 42L))
                .andExpect(status().isNoContent());

        verify(service).toggleActiveStatus(42L);
    }
    @Test
    void testToggleActiveStatus_NotFound() throws Exception {
        mockMvc.perform(patch("/api/setup/user-facility-departments/{id}/toggle", 9999L))
                .andExpect(status().isNoContent());

        verify(service).toggleActiveStatus(9999L);
    }
    @Test
    void testGetByUser() throws Exception {
        var r1 = new UserFacilityDepartmentResponseVM(1L, 5L, 2L, 10L, true);
        var r2 = new UserFacilityDepartmentResponseVM(2L, 5L, 3L, 12L, false);
        when(service.getUserFacilityDepartmentsByUser(5L)).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/setup/user-facility-departments/user/{userId}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value(5))
                .andExpect(jsonPath("$[0].facilityId").value(2))
                .andExpect(jsonPath("$[0].departmentId").value(10))
                .andExpect(jsonPath("$[0].isActive").value(true))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].facilityId").value(3))
                .andExpect(jsonPath("$[1].departmentId").value(12))
                .andExpect(jsonPath("$[1].isActive").value(false));
    }
    @Test
    void testGetByUser_NotFound() throws Exception {
        when(service.getUserFacilityDepartmentsByUser(9999L)).thenReturn(List.of());

        mockMvc.perform(get("/api/setup/user-facility-departments/user/{userId}", 9999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }
    @Test
    void testExists_true() throws Exception {
        when(service.exists( eq(5L), eq(10L))).thenReturn(true);

        mockMvc.perform(get("/api/setup/user-facility-departments/exists")
                        .param("userId", "5")
                        .param("departmentId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testExists_false() throws Exception {
        when(service.exists( eq(5L), eq(11L))).thenReturn(false);

        mockMvc.perform(get("/api/setup/user-facility-departments/exists")
                        .param("userId", "5")
                        .param("departmentId", "11"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
