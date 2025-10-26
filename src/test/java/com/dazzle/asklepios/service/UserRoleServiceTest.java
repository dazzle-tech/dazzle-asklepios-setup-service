package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.UserRole;
import com.dazzle.asklepios.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.doNothing;


class UserRoleServiceTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private UserRoleService userRoleService;

    private UserRole ur1;
    private UserRole ur2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ur1 = UserRole.builder()
                .id(new UserRole.UserRoleId(1L, 10L))
                .build();

        ur2 = UserRole.builder()
                .id(new UserRole.UserRoleId(2L, 20L))
                .build();
    }

    @Test
    void testFindAll_Success() {
        when(userRoleRepository.findAll()).thenReturn(List.of(ur1, ur2));

        List<UserRole> result = userRoleService.findAll();

        assertThat(result).hasSize(2).containsExactly(ur1, ur2);
        verify(userRoleRepository).findAll();
        verifyNoMoreInteractions(userRoleRepository);
    }

    @Test
    void testFindByUserId_Success() {
        when(userRoleRepository.findByIdUserId(1L)).thenReturn(List.of(ur1));

        List<UserRole> result = userRoleService.findByUserId(1L);

        assertThat(result).hasSize(1).containsExactly(ur1);
        verify(userRoleRepository).findByIdUserId(1L);
        verifyNoMoreInteractions(userRoleRepository);
    }

    @Test
    void testFindByRoleId_Success() {
        when(userRoleRepository.findByIdRoleId(20L)).thenReturn(List.of(ur2));

        List<UserRole> result = userRoleService.findByRoleId(20L);

        assertThat(result).hasSize(1).containsExactly(ur2);
        verify(userRoleRepository).findByIdRoleId(20L);
        verifyNoMoreInteractions(userRoleRepository);
    }

    @Test
    void testSave_Success() {
        when(userRoleRepository.save(any(UserRole.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserRole result = userRoleService.save(3L, 30L);

        assertThat(result.getId().getUserId()).isEqualTo(3L);
        assertThat(result.getId().getRoleId()).isEqualTo(30L);
        verify(userRoleRepository).save(any(UserRole.class));
        verifyNoMoreInteractions(userRoleRepository);
    }

    @Test
    void testDelete_Success() {
        doNothing().when(userRoleRepository).deleteById(any(UserRole.UserRoleId.class));

        userRoleService.delete(1L, 10L);

        verify(userRoleRepository).deleteById(new UserRole.UserRoleId(1L, 10L));
        verifyNoMoreInteractions(userRoleRepository);
    }
}
