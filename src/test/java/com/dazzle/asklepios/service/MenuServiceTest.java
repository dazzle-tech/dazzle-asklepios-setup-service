package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import com.dazzle.asklepios.repository.MenuRepository;
import com.dazzle.asklepios.web.rest.vm.MenuItemVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;

class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuService menuService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetMenu_OrderAndMergeOperations() {
        // Mock MenuRow using Mockito (MenuRow is abstract)
        MenuRepository.MenuRow row1 = mock(MenuRepository.MenuRow.class);
        when(row1.getScreen()).thenReturn("SCHEDULING_SCREEN");
        when(row1.getOperation()).thenReturn("VIEW");

        MenuRepository.MenuRow row2 = mock(MenuRepository.MenuRow.class);
        when(row2.getScreen()).thenReturn("DEPARTMENTS");
        when(row2.getOperation()).thenReturn("EDIT");

        MenuRepository.MenuRow row3 = mock(MenuRepository.MenuRow.class);
        when(row3.getScreen()).thenReturn("DEPARTMENTS");
        when(row3.getOperation()).thenReturn("VIEW");

        when(menuRepository.findScreensForUserAndFacility(1L, 1L))
                .thenReturn(List.of(row1, row2, row3));

        List<MenuItemVM> menu = menuService.getMenu(1L, 1L);

        assertThat(menu).hasSize(2);
        assertThat(menu.get(0).screen()).isEqualTo(Screen.DEPARTMENTS);
        assertThat(menu.get(1).screen()).isEqualTo(Screen.SCHEDULING_SCREEN);

        EnumSet<Operation> departmentsOps = menu.get(0).operations();
        assertThat(departmentsOps).containsExactlyInAnyOrder(Operation.VIEW, Operation.EDIT);

        EnumSet<Operation> schedulingOps = menu.get(1).operations();
        assertThat(schedulingOps).containsExactly(Operation.VIEW);

        verify(menuRepository, times(1)).findScreensForUserAndFacility(1L, 1L);
    }

    @Test
    void testGetMenu_InvalidScreenOrOperation() {
        // Mock invalid values
        MenuRepository.MenuRow row1 = mock(MenuRepository.MenuRow.class);
        when(row1.getScreen()).thenReturn("INVALID_SCREEN");
        when(row1.getOperation()).thenReturn("VIEW");

        MenuRepository.MenuRow row2 = mock(MenuRepository.MenuRow.class);
        when(row2.getScreen()).thenReturn("DEPARTMENTS");
        when(row2.getOperation()).thenReturn("INVALID_OP");

        when(menuRepository.findScreensForUserAndFacility(1L, 1L))
                .thenReturn(List.of(row1, row2));

        List<MenuItemVM> menu = menuService.getMenu(1L, 1L);

        assertThat(menu).isEmpty();
    }

    @Test
    void testGetMenu_NoAuthorities() {
        when(menuRepository.findScreensForUserAndFacility(1L, 1L))
                .thenReturn(List.of());

        List<MenuItemVM> menu = menuService.getMenu(1L, 1L);

        assertThat(menu).isEmpty();
    }
}
