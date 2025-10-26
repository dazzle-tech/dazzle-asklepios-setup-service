package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Role;
import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import com.dazzle.asklepios.repository.RoleRepository;
import com.dazzle.asklepios.service.RolePermissionService;
import com.dazzle.asklepios.service.RoleService;
import com.dazzle.asklepios.web.rest.vm.RoleCreateVM;
import com.dazzle.asklepios.web.rest.vm.RoleScreenVM;
import com.dazzle.asklepios.web.rest.vm.RoleUpdateVM;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RolePermissionService rolePermissionService;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Role role;
    private Facility facility;

    @BeforeEach
    void setUp() {
        facility = new Facility();
        facility.setId(1L);
        facility.setName("Main Hospital");

        role = new Role();
        role.setId(10L);
        role.setName("Doctor");
        role.setType("MEDICAL");
        role.setFacility(facility);
    }

    @Test
    void testCreateRole_Success() throws Exception {
        RoleCreateVM createVM = new RoleCreateVM("Doctor", "MEDICAL", 1L);

        when(roleRepository.existsByNameIgnoreCase("Doctor")).thenReturn(false);
        when(roleService.create(any(RoleCreateVM.class))).thenReturn(role);

        mockMvc.perform(post("/api/setup/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVM)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Doctor"))
                .andExpect(jsonPath("$.facilityName").value("Main Hospital"));
    }

    @Test
    void testCreateRole_Conflict_WhenNameExists() throws Exception {
        RoleCreateVM createVM = new RoleCreateVM("Doctor", "MEDICAL", 1L);
        when(roleRepository.existsByNameIgnoreCase("Doctor")).thenReturn(true);

        mockMvc.perform(post("/api/setup/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVM)))
                .andExpect(status().isConflict());
    }

    @Test
    void testUpdateRole_Success() throws Exception {
        RoleUpdateVM updateVM = new RoleUpdateVM(10L, "Updated Doctor", "ADMIN");

        Role updated = new Role();
        updated.setId(10L);
        updated.setName("Updated Doctor");
        updated.setType("ADMIN");
        updated.setFacility(facility);

        when(roleService.update(eq(10L), any(RoleUpdateVM.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/setup/role/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateVM)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Doctor"))
                .andExpect(jsonPath("$.facilityName").value("Main Hospital"));
    }

    @Test
    void testUpdateRole_NotFound() throws Exception {
        RoleUpdateVM updateVM = new RoleUpdateVM(999L, "X", "ADMIN");
        when(roleService.update(eq(999L), any(RoleUpdateVM.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/setup/role/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateVM)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllRoles() throws Exception {
        when(roleService.findAll()).thenReturn(List.of(role));

        mockMvc.perform(get("/api/setup/role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Doctor"))
                .andExpect(jsonPath("$[0].facilityName").value("Main Hospital"));
    }

    @Test
    void testGetRoleById_Found() throws Exception {
        when(roleService.findOne(10L)).thenReturn(Optional.of(role));

        mockMvc.perform(get("/api/setup/role/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Doctor"));
    }

    @Test
    void testGetRoleById_NotFound() throws Exception {
        when(roleService.findOne(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/setup/role/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteRole_Success() throws Exception {
        when(roleService.delete(10L)).thenReturn(true);

        mockMvc.perform(delete("/api/setup/role/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteRole_NotFound() throws Exception {
        when(roleService.delete(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/setup/role/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetRolesByFacility() throws Exception {
        when(roleService.findByFacilityId(1L)).thenReturn(List.of(role));

        mockMvc.perform(get("/api/setup/role/facility/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].facilityName").value("Main Hospital"));
    }

    @Test
    void testUpdateRoleScreens_Success() throws Exception {
        List<RoleScreenVM> screens = List.of(
                new RoleScreenVM(Screen.DEPARTMENTS, Operation.VIEW),
                new RoleScreenVM(Screen.AGE_GROUP, Operation.EDIT)
        );
        doNothing().when(rolePermissionService).updateRolePermissions(eq(10L), any());

        mockMvc.perform(put("/api/setup/role/10/screens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(screens)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateRoleScreens_InvalidRequest() throws Exception {
        List<RoleScreenVM> screens = List.of(
                new RoleScreenVM(Screen.DEPARTMENTS, null) // invalid
        );

        mockMvc.perform(put("/api/setup/role/10/screens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(screens)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testGetRoleScreens_Success() throws Exception {
        List<RoleScreenVM> screens = List.of(
                new RoleScreenVM(Screen.DEPARTMENTS, Operation.VIEW)
        );
        when(roleRepository.existsById(10L)).thenReturn(true);
        when(rolePermissionService.getRoleScreens(10L)).thenReturn(screens);

        mockMvc.perform(get("/api/setup/role/10/screens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].screen").value("DEPARTMENTS"));
    }

    @Test
    void testGetRoleScreens_NotFound() throws Exception {
        when(roleRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(get("/api/setup/role/99/screens"))
                .andExpect(status().isNotFound());
    }
}
