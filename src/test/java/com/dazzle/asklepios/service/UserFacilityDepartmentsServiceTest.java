package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.UserFacilityDepartment;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.UserFacilityDepartmentRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.UserFacilityDepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.UserFacilityDepartmentResponseVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserFacilityDepartmentsServiceTest {

    @Mock
    private UserFacilityDepartmentRepository ufdRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentsRepository departmentRepository;

    @InjectMocks
    private UserFacilityDepartmentService service;

    private User user;
    private Department department;
    private UserFacilityDepartment existing;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(5L);

        department = new Department();
        department.setId(10L);

        existing = UserFacilityDepartment.builder()
                .id(23L)
                .user(user)
                .department(department)
                .isActive(true)
                .build();
    }

    @Test
    void testCreate_ReturnsExistingIfPresent() {
        var vm = new UserFacilityDepartmentCreateVM(
                user.getId(), department.getId(),
                true, "creator", Instant.parse("2024-01-01T00:00:00Z")
        );

        when(ufdRepository.findByUserIdAndDepartmentId(2L,  10L))
                .thenReturn(Optional.of(existing));

        UserFacilityDepartmentResponseVM out = service.createUserFacilityDepartment(vm);

        assertThat(out.id()).isEqualTo(23L);
        assertThat(out.userId()).isEqualTo(5L);
        assertThat(out.departmentId()).isEqualTo(10L);
        assertThat(out.isActive()).isTrue();

        verifyNoInteractions(userRepository, departmentRepository);
        verify(ufdRepository, never()).save(any());
    }

    @Test
    void testCreate_PersistsNew_WhenRefsExist() {
        var vm = new UserFacilityDepartmentCreateVM(
                5L,  10L,
                null, "creator", Instant.parse("2024-01-01T00:00:00Z")
        );

        when(ufdRepository.findByUserIdAndDepartmentId(2L,  10L)).thenReturn(Optional.empty());
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));

        ArgumentCaptor<UserFacilityDepartment> captor = ArgumentCaptor.forClass(UserFacilityDepartment.class);
        when(ufdRepository.save(captor.capture())).thenAnswer(inv -> {
            UserFacilityDepartment saved = inv.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        UserFacilityDepartmentResponseVM out = service.createUserFacilityDepartment(vm);

        assertThat(out.id()).isEqualTo(99L);
        assertThat(out.userId()).isEqualTo(5L);
        assertThat(out.facilityId()).isEqualTo(2L);
        assertThat(out.departmentId()).isEqualTo(10L);
        assertThat(out.isActive()).isTrue(); // default applied

        UserFacilityDepartment saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getDepartment()).isEqualTo(department);
        assertThat(saved.getCreatedBy()).isEqualTo("creator");
        assertThat(saved.getCreatedDate()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
        assertThat(saved.getIsActive()).isTrue();
    }


    @Test
    void testCreate_UserNotFound() {
        var vm = new UserFacilityDepartmentCreateVM(2L, 10L, true, "creator", null);

        when(ufdRepository.findByUserIdAndDepartmentId(5L, 10L)).thenReturn(Optional.empty());
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(BadRequestAlertException.class, () -> service.createUserFacilityDepartment(vm));
        verifyNoInteractions(departmentRepository);
        verify(ufdRepository, never()).save(any());
    }

    @Test
    void testCreate_DepartmentNotFound() {
        var vm = new UserFacilityDepartmentCreateVM( 2L, 10L, true, "creator", null);

        when(ufdRepository.findByUserIdAndDepartmentId( 5L, 10L)).thenReturn(Optional.empty());
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(departmentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(BadRequestAlertException.class, () -> service.createUserFacilityDepartment(vm));
        verify(ufdRepository, never()).save(any());
    }

    @Test
    void testToggleActiveStatus_TogglesAndSetsLastModified() {
        UserFacilityDepartment e = UserFacilityDepartment.builder()
                .id(50L).user(user).department(department).isActive(true).build();

        when(ufdRepository.findById(50L)).thenReturn(Optional.of(e));
        when(ufdRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.toggleActiveStatus(50L);

        assertThat(e.getIsActive()).isFalse();
        assertThat(e.getLastModifiedDate()).isNotNull();
        verify(ufdRepository).save(e);
    }

    @Test
    void testToggleActiveStatus_NoEntity_NoSave() {
        when(ufdRepository.findById(77L)).thenReturn(Optional.empty());

        service.toggleActiveStatus(77L);

        verify(ufdRepository, never()).save(any());
    }

    @Test
    void testGetByUser_ReturnsResponseVMs() {
        var e1 = UserFacilityDepartment.builder().id(1L).user(user).department(department).isActive(true).build();
        var e2 = UserFacilityDepartment.builder().id(2L).user(user).department(department).isActive(false).build();

        when(ufdRepository.findByUserId(5L)).thenReturn(List.of(e1, e2));

        List<UserFacilityDepartmentResponseVM> out = service.getUserFacilityDepartmentsByUser(5L);

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
        assertThat(service.exists(5L, 10L)).isTrue();

        when(ufdRepository.findByUserIdAndDepartmentId(2L,  11L)).thenReturn(Optional.empty());
        assertThat(service.exists( 5L, 11L)).isFalse();
    }
}
