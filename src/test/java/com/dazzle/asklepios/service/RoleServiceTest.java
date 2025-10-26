package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Role;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.RoleRepository;
import com.dazzle.asklepios.web.rest.vm.RoleCreateVM;
import com.dazzle.asklepios.web.rest.vm.RoleUpdateVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doNothing;


class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @InjectMocks
    private RoleService roleService;

    private Facility facility;
    private Role role;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        facility = Facility.builder()
                .id(1L)
                .name("Hospital A")
                .build();

        role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .type("SYSTEM")
                .facility(facility)
                .build();
    }

    @Test
    void testCreateRole_Success() {
        RoleCreateVM vm = new RoleCreateVM("ADMIN", "SYSTEM", facility.getId());

        when(facilityRepository.findById(facility.getId())).thenReturn(Optional.of(facility));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        Role created = roleService.create(vm);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("ADMIN");
        assertThat(created.getFacility()).isEqualTo(facility);

        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void testCreateRole_FacilityNotFound() {
        RoleCreateVM vm = new RoleCreateVM("ADMIN", "SYSTEM", 99L);

        when(facilityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> roleService.create(vm));

        verify(roleRepository, never()).save(any());
    }

    @Test
    void testUpdateRole_Success() {
        RoleUpdateVM vm = new RoleUpdateVM(role.getId(), "MANAGER", "SYSTEM");

        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));
        when(roleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Role> updated = roleService.update(vm.id(), vm);

        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("MANAGER");
        assertThat(updated.get().getType()).isEqualTo("SYSTEM");

        verify(roleRepository).save(any(Role.class));
    }


    @Test
    void testFindAllRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(role));

        List<Role> result = roleService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(role);
    }

    @Test
    void testFindOneRole_Found() {
        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));

        Optional<Role> result = roleService.findOne(role.getId());

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(role);
    }

    @Test
    void testFindOneRole_NotFound() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Role> result = roleService.findOne(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void testFindByFacilityId() {
        when(roleRepository.findByFacilityId(facility.getId())).thenReturn(List.of(role));

        List<Role> result = roleService.findByFacilityId(facility.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(role);
    }

    @Test
    void testDeleteRole_Success() {
        when(roleRepository.existsById(role.getId())).thenReturn(true);
        doNothing().when(roleRepository).deleteById(role.getId());

        boolean deleted = roleService.delete(role.getId());

        assertThat(deleted).isTrue();
        verify(roleRepository).deleteById(role.getId());
    }

    @Test
    void testDeleteRole_NotFound() {
        when(roleRepository.existsById(99L)).thenReturn(false);

        boolean deleted = roleService.delete(99L);

        assertThat(deleted).isFalse();
        verify(roleRepository, never()).deleteById(anyLong());
    }
}
