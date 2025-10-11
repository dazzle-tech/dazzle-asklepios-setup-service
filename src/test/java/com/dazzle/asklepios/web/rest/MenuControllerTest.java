package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.service.MenuService;
import com.dazzle.asklepios.web.rest.vm.MenuItemVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class MenuControllerTest {

    @Mock
    private MenuService menuService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MenuController menuController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getMenu_success() {
        String login = "alice";
        Long facilityId = 99L;
        Long userId = 123L;

        try (MockedStatic<SecurityUtils> sec = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(login));
            sec.when(SecurityUtils::getCurrentUserFacility).thenReturn(Optional.of(facilityId));

            User u = new User();
            u.setId(userId);
            u.setLogin(login);
            when(userRepository.findByLogin(login)).thenReturn(Optional.of(u));

            List<MenuItemVM> items = List.of(
                    MenuItemVM.of(Screen.DEPARTMENTS, EnumSet.of(Operation.VIEW, Operation.EDIT)),
                    MenuItemVM.of(Screen.AGE_GROUP, EnumSet.of(Operation.VIEW))
            );
            when(menuService.getMenu(eq(userId), eq(facilityId))).thenReturn(items);

            var response = menuController.getMenu();
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody().get(0).screen()).isEqualTo(Screen.DEPARTMENTS);
            assertThat(response.getBody().get(0).operations()).contains(Operation.VIEW, Operation.EDIT);
            assertThat(response.getBody().get(1).screen()).isEqualTo(Screen.AGE_GROUP);
            assertThat(response.getBody().get(1).operations()).contains(Operation.VIEW);
        }
    }

    @Test
    void getMenu_noLogin_throws401() {
        try (MockedStatic<SecurityUtils> sec = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.empty());

            assertThatThrownBy(() -> menuController.getMenu())
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not authenticated");
        }
    }

    @Test
    void getMenu_userNotFound_throws401() {
        String login = "ghost";
        try (MockedStatic<SecurityUtils> sec = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(login));
            when(userRepository.findByLogin(login)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> menuController.getMenu())
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Test
    void getMenu_missingFacility_throws401() {
        String login = "alice";

        try (MockedStatic<SecurityUtils> sec = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(login));
            sec.when(SecurityUtils::getCurrentUserFacility).thenReturn(Optional.empty());

            User u = new User();
            u.setId(123L);
            u.setLogin(login);
            when(userRepository.findByLogin(login)).thenReturn(Optional.of(u));

            assertThatThrownBy(() -> menuController.getMenu())
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Missing mandatory claim 'tenant'");
        }
    }
}
