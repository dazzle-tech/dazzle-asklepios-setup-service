package com.dazzle.asklepios.service;


import com.dazzle.asklepios.domain.Role;
import com.dazzle.asklepios.domain.RoleScreen;
import com.dazzle.asklepios.domain.RoleScreenId;
import com.dazzle.asklepios.domain.ScreenAuthority;
import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import com.dazzle.asklepios.repository.RoleAuthorityRepository;
import com.dazzle.asklepios.repository.RoleRepository;
import com.dazzle.asklepios.repository.RoleScreenRepository;
import com.dazzle.asklepios.repository.ScreenAuthorityRepository;
import com.dazzle.asklepios.web.rest.vm.RoleScreenVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;


class RolePermissionServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleScreenRepository roleScreenRepository;

    @Mock
    private RoleAuthorityRepository roleAuthorityRepository;

    @Mock
    private ScreenAuthorityRepository screenAuthorityRepository;

    @InjectMocks
    private RolePermissionService rolePermissionService;

    private Role role;
    private ScreenAuthority screenAuthority;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        role = Role.builder()
                .id(1L)
                .name("Admin")
                .type("SYSTEM")
                .build();

        screenAuthority = ScreenAuthority.builder()
                .screen(Screen.DEPARTMENTS)
                .operation(Operation.VIEW)
                .authorityName("DASHBOARD_READ")
                .build();
    }

    @Test
    void testUpdateRolePermissions_Success() {
        List<RoleScreenVM> requests = List.of(
                new RoleScreenVM(Screen.DEPARTMENTS, Operation.VIEW)
        );

        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));
        when(screenAuthorityRepository.findAll()).thenReturn(List.of(screenAuthority));
        doNothing().when(roleScreenRepository).deleteByIdRoleId(role.getId());
        doNothing().when(roleAuthorityRepository).deleteByRoleId(role.getId());
        when(roleScreenRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleAuthorityRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        rolePermissionService.updateRolePermissions(role.getId(), requests);

        verify(roleScreenRepository).deleteByIdRoleId(role.getId());
        verify(roleAuthorityRepository).deleteByRoleId(role.getId());
        verify(roleScreenRepository).saveAll(anyList());
        verify(roleAuthorityRepository).saveAll(anyList());
    }

    @Test
    void testUpdateRolePermissions_RoleNotFound() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        List<RoleScreenVM> requests = List.of(new RoleScreenVM(Screen.DEPARTMENTS, Operation.VIEW));

        assertThrows(RuntimeException.class,
                () -> rolePermissionService.updateRolePermissions(99L, requests));
    }

    @Test
    void testGetRoleScreens_Success() {
        RoleScreen roleScreen = RoleScreen.builder()
                .id(new RoleScreenId(role.getId(), Screen.DEPARTMENTS, Operation.VIEW))
                .role(role)
                .build();

        when(roleRepository.existsById(role.getId())).thenReturn(true);
        when(roleScreenRepository.findByIdRoleId(role.getId())).thenReturn(List.of(roleScreen));

        List<RoleScreenVM> result = rolePermissionService.getRoleScreens(role.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).screen()).isEqualTo(Screen.DEPARTMENTS);
        assertThat(result.get(0).permission()).isEqualTo(Operation.VIEW);
    }

    @Test
    void testGetRoleScreens_RoleNotFound() {
        when(roleRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> rolePermissionService.getRoleScreens(99L));
    }

    @Test
    void testGetRoleScreens_TooManyRecords() {
        when(roleRepository.existsById(role.getId())).thenReturn(true);

        List<RoleScreen> manyScreens = new ArrayList<>();
        for (int i = 0; i < 2500; i++) {
            manyScreens.add(RoleScreen.builder()
                    .id(new RoleScreenId(role.getId(), Screen.DEPARTMENTS, Operation.VIEW))
                    .role(role)
                    .build());
        }

        when(roleScreenRepository.findByIdRoleId(role.getId())).thenReturn(manyScreens);

        assertThrows(ResponseStatusException.class,
                () -> rolePermissionService.getRoleScreens(role.getId()));
    }
}
