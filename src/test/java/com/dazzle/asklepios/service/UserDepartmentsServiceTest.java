package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.UserDepartment;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.UserDepartmentRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.userDepartments.UserDepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.userDepartments.UserDepartmentResponseVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserDepartmentsServiceTest {

    @Mock
    private UserDepartmentRepository ufdRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentsRepository departmentRepository;

    @InjectMocks
    private UserDepartmentService service;

    private User user;
    private Department department;
    private UserDepartment existing;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(5L);

        department = new Department();
        department.setId(10L);

        existing = UserDepartment.builder()
                .id(23L)
                .user(user)
                .department(department)
                .isActive(true)
                .build();
    }

    @Test
    void testCreate_ReturnsExistingIfPresent() {
        var vm = new UserDepartmentCreateVM(
                user.getId(), department.getId(),
                true
        );

        when(ufdRepository.findByUserIdAndDepartmentId(1L,  5001L))
                .thenReturn(Optional.of(existing));

        UserDepartment out = service.createUserDepartment(vm);

        assertThat(out.getId()).isEqualTo(2L);
        assertThat(out.getUser().getId()).isEqualTo(1L);
        assertThat(out.getDepartment().getId()).isEqualTo(5001L);

        verifyNoInteractions( userRepository, departmentRepository);
        verify(ufdRepository, never()).save(any());
    }

    @Test
    void testCreate_PersistsNew_WhenRefsExist() {
        var vm = new UserDepartmentCreateVM(
                5L,  10L,
                null
        );

        when(ufdRepository.findByUserIdAndDepartmentId(2L,10L)).thenReturn(Optional.empty());
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));

        ArgumentCaptor<UserDepartment> captor = ArgumentCaptor.forClass(UserDepartment.class);
        when(ufdRepository.save(captor.capture())).thenAnswer(inv -> {
            UserDepartment saved = inv.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        UserDepartment out = service.createUserDepartment(vm);

        assertThat(out.getId()).isEqualTo(99L);
        assertThat(out.getUser().getId()).isEqualTo(5L);
        assertThat(out.getDepartment().getId()).isEqualTo(10L);
        assertThat(out.getIsActive()).isTrue(); // default applied

        UserDepartment saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getDepartment()).isEqualTo(department);
        assertThat(saved.getIsActive()).isTrue();
    }


    @Test
    void testCreate_UserNotFound() {
        var vm = new UserDepartmentCreateVM(5L, 10L, true);

        when(ufdRepository.findByUserIdAndDepartmentId(2L, 10L)).thenReturn(Optional.empty());
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(BadRequestAlertException.class, () -> service.createUserDepartment(vm));
        verifyNoInteractions(departmentRepository);
        verify(ufdRepository, never()).save(any());
    }

    @Test
    void testCreate_DepartmentNotFound() {
        var vm = new UserDepartmentCreateVM(5L,  10L, true);

        when(ufdRepository.findByUserIdAndDepartmentId(2L,  10L)).thenReturn(Optional.empty());
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(departmentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(BadRequestAlertException.class, () -> service.createUserDepartment(vm));
        verify(ufdRepository, never()).save(any());
    }

    @Test
    void testGetByUser_ReturnsResponseVMs() {
        var e1 = UserDepartment.builder().id(1L).user(user).department(department).isActive(true).build();
        var e2 = UserDepartment.builder().id(2L).user(user).department(department).isActive(false).build();

        when(ufdRepository.findByUserId(5L)).thenReturn(List.of(e1, e2));

        List<UserDepartmentResponseVM> out = service.getUserDepartmentsByUser(5L) .stream()
                .map(UserDepartmentResponseVM::ofEntity)
                .toList();;

        assertThat(out).hasSize(2);
        assertThat(out.get(0).id()).isEqualTo(1L);
        assertThat(out.get(0).userId()).isEqualTo(5L);
        assertThat(out.get(0).departmentId()).isEqualTo(10L);
        assertThat(out.get(0).isActive()).isTrue();

        assertThat(out.get(1).id()).isEqualTo(2L);
        assertThat(out.get(1).isActive()).isFalse();

        verify(ufdRepository).findByUserId(5L);
    }

    @Test
    void testExists_DelegatesToRepository() {
        when(ufdRepository.findByUserIdAndDepartmentId(2L,  10L)).thenReturn(Optional.of(existing));
        assertThat(service.exists(2L,  10L)).isTrue();

        when(ufdRepository.findByUserIdAndDepartmentId(2L,  11L)).thenReturn(Optional.empty());
        assertThat(service.exists(2L,  11L)).isFalse();
    }
}