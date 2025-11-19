package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.config.TestSecurityConfig;
import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.UserDepartment;
import com.dazzle.asklepios.service.UserDepartmentService;
import com.dazzle.asklepios.web.rest.vm.userDepartments.UserDepartmentResponseVM;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserDepartmentController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class UserDepartmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserDepartmentService service;

    private static User user(long id){ var u=new User(); u.setId(id); return u; }
    private static Department dept(long id){ var d=new Department(); d.setId(id); return d; }

    @Test
    void testCreateUserDepartment() throws Exception {
        var saved = UserDepartment.builder()
                .id(23L).user(user(5L)).department(dept(10L)).isActive(true).build();
        when(service.createUserDepartment(any())).thenReturn(saved);

        mockMvc.perform(post("/api/setup/user-departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"userId":5,"departmentId":10,"isActive":true}
                    """))
                .andExpect(status().isCreated())
                // controller sets Location to /api/user-facility-departments/{id}
                .andExpect(header().string("Location", endsWith("/api/user-facility-departments/23")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(23))
                .andExpect(jsonPath("$.user.id").value(5))
                .andExpect(jsonPath("$.department.id").value(10))
                .andExpect(jsonPath("$.isActive").value(true));
    }



    @Test
    void testGetByUser() throws Exception {
        // given
        UserDepartmentResponseVM r2 = new UserDepartmentResponseVM(
                5L,    // id
                2L,    // userId
                10L,   // facilityId (any test value)
                5001L, // departmentId
                false, // isActive
                false  // isDefault
        );

        when(service.getUserDepartmentsByUser(2L))
                .thenReturn(List.of(r2));

        // when + then
        mockMvc.perform(get("/api/setup/user-departments/user/{userId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].userId").value(2))
                .andExpect(jsonPath("$[0].departmentId").value(5001))
                .andExpect(jsonPath("$[0].isActive").value(false));
    }


    @Test
    void testGetByUser_NotFound() throws Exception {
        when(service.getUserDepartmentsByUser(9999L)).thenReturn(List.of());

        mockMvc.perform(get("/api/setup/user-departments/user/{userId}", 9999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testExists_true() throws Exception {
        when(service.exists(eq(5L), eq(10L))).thenReturn(true);

        mockMvc.perform(get("/api/setup/user-departments/exists")
                        .param("userId", "5")
                        .param("departmentId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testExists_false() throws Exception {
        when(service.exists(eq(5L), eq(11L))).thenReturn(false);

        mockMvc.perform(get("/api/setup/user-departments/exists")
                        .param("userId", "5")
                        .param("departmentId", "11"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}