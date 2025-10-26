package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.UserRole;
import com.dazzle.asklepios.service.UserRoleService;
import com.dazzle.asklepios.web.rest.vm.UserRoleVM;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = UserRoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRoleService userRoleService;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper method to create a UserRole with embedded ID
    private UserRole ur(long userId, long roleId) {
        UserRole entity = new UserRole();
        UserRole.UserRoleId id = new UserRole.UserRoleId(userId, roleId);
        entity.setId(id);
        return entity;
    }

    @BeforeEach
    void setup() {
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(userRoleService.findAll()).thenReturn(List.of(
                ur(1L, 10L),
                ur(2L, 20L)
        ));

        mockMvc.perform(get("/api/setup/user-role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].roleId").value(10))
                .andExpect(jsonPath("$[1].userId").value(2))
                .andExpect(jsonPath("$[1].roleId").value(20));
    }

    @Test
    void getByUser_shouldReturnUserRoles() throws Exception {
        when(userRoleService.findByUserId(7L)).thenReturn(List.of(
                ur(7L, 100L),
                ur(7L, 200L)
        ));

        mockMvc.perform(get("/api/setup/user-role/by-user/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(7))
                .andExpect(jsonPath("$[0].roleId").value(100))
                .andExpect(jsonPath("$[1].userId").value(7))
                .andExpect(jsonPath("$[1].roleId").value(200));
    }

    @Test
    void getByRole_shouldReturnUsersForRole() throws Exception {
        when(userRoleService.findByRoleId(55L)).thenReturn(List.of(
                ur(3L, 55L),
                ur(4L, 55L)
        ));

        mockMvc.perform(get("/api/setup/user-role/55"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(3))
                .andExpect(jsonPath("$[0].roleId").value(55))
                .andExpect(jsonPath("$[1].userId").value(4))
                .andExpect(jsonPath("$[1].roleId").value(55));
    }

    @Test
    void add_shouldReturnSavedMapping() throws Exception {
        when(userRoleService.save(9L, 88L)).thenReturn(ur(9L, 88L));

        var body = new UserRoleVM(9L, 88L);

        mockMvc.perform(post("/api/setup/user-role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(9))
                .andExpect(jsonPath("$.roleId").value(88));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        doNothing().when(userRoleService).delete(anyLong(), anyLong());

        var body = new UserRoleVM(12L, 34L);

        mockMvc.perform(delete("/api/setup/user-role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNoContent());
    }
}
