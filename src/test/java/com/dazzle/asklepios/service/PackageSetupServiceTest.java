package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PackageSetup;
import com.dazzle.asklepios.domain.PackageSetupItem;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.repository.DiscountRepository;
import com.dazzle.asklepios.repository.PackageSetupDepartmentRepository;
import com.dazzle.asklepios.repository.PackageSetupItemRepository;
import com.dazzle.asklepios.repository.PackageSetupRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.packageSetup.PackageItemLookupVM;
import com.dazzle.asklepios.web.rest.vm.packageSetup.PackageSetupItemVM;
import com.dazzle.asklepios.web.rest.vm.packageSetup.PackageSetupResponseVM;
import com.dazzle.asklepios.web.rest.vm.packageSetup.PackageSetupSaveVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Rules of the package setup screen: dates, departments, items, category lines, discount with a fixed price. */
class PackageSetupServiceTest {

    @Mock private PackageSetupRepository packageSetupRepository;
    @Mock private PackageSetupItemRepository packageSetupItemRepository;
    @Mock private PackageItemLookupService packageItemLookupService;
    @Mock private PackageSetupDepartmentRepository packageSetupDepartmentRepository;
    @Mock private DiscountRepository discountRepository;

    private PackageSetupService service;
    private final List<PackageSetupItem> savedItems = new ArrayList<>();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PackageSetupService(packageSetupRepository, packageSetupItemRepository,
                packageItemLookupService, packageSetupDepartmentRepository, discountRepository);

        when(packageSetupRepository.existsByCodeIgnoreCase(anyString())).thenReturn(false);
        when(packageSetupRepository.saveAndFlush(any())).thenAnswer(i -> {
            PackageSetup p = i.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(packageSetupItemRepository.saveAllAndFlush(anyList())).thenAnswer(i -> {
            savedItems.addAll((List<PackageSetupItem>) i.getArgument(0));
            return i.getArgument(0);
        });
        when(packageSetupItemRepository.findByPackageSetupIdOrderByIdAsc(anyLong())).thenAnswer(i -> savedItems);
        when(packageSetupDepartmentRepository.findByPackageSetupIdIn(anyCollection())).thenReturn(List.of());
        // every looked-up item exists, named after its id
        when(packageItemLookupService.findByIds(any(), anyCollection())).thenAnswer(i -> {
            PriceListItemType type = i.getArgument(0);
            Collection<Long> ids = i.getArgument(1);
            Map<Long, PackageItemLookupVM> found = new HashMap<>();
            ids.forEach(id -> found.put(id, new PackageItemLookupVM(type, id, "C" + id, "Item " + id, BigDecimal.TEN)));
            return found;
        });
    }

    private static PackageSetupItemVM item(PriceListItemType type, Long sourceId, int qty) {
        return new PackageSetupItemVM(null, type, sourceId, null, null, qty, true, false, null, null, null);
    }

    private static PackageSetupItemVM category(PriceListItemType type, int qty, Integer repeat, String discount) {
        return new PackageSetupItemVM(null, type, null, null, null, qty, true, true, repeat,
                discount == null ? null : new BigDecimal(discount), null);
    }

    private static PackageSetupSaveVM pkg(BigDecimal price, LocalDate from, LocalDate to, Boolean allDepartments,
                                          List<Long> departments, List<PackageSetupItemVM> items) {
        return new PackageSetupSaveVM("PKG-T", "Test", null, price, true, from, to, null, allDepartments, departments, items);
    }

    @Test
    void categoryAndItemLinesAreSavedWithTheirRules() {
        PackageSetupResponseVM saved = service.create(pkg(null, null, null, true, null, List.of(
                category(PriceListItemType.LABORATORY, 5, 1, "50"),
                item(PriceListItemType.SERVICE, 1501L, 2))));

        assertThat(saved.items()).hasSize(2);
        PackageSetupItem anyLab = savedItems.get(0);
        assertThat(anyLab.getIsCategory()).isTrue();
        assertThat(anyLab.getSourceId()).isNull();
        assertThat(anyLab.getMaxRepeat()).isEqualTo(1);
        assertThat(anyLab.getDiscountPercent()).isEqualByComparingTo("50");
        PackageSetupItem dental = savedItems.get(1);
        assertThat(dental.getItemName()).isEqualTo("Item 1501");
        assertThat(dental.getMaxRepeat()).isNull();
    }

    @Test
    void fixedPriceWithItemDiscountIsRefused() {
        assertThatThrownBy(() -> service.create(pkg(new BigDecimal("300"), null, null, true, null,
                List.of(category(PriceListItemType.LABORATORY, 5, null, "20")))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("fixed price");
        verify(packageSetupRepository, never()).saveAndFlush(any());
    }

    @Test
    void fixedPriceWithoutDiscountIsAccepted() {
        PackageSetupResponseVM saved = service.create(pkg(new BigDecimal("300"), null, null, true, null,
                List.of(category(PriceListItemType.LABORATORY, 8, 2, null))));

        assertThat(saved.price()).isEqualByComparingTo("300");
    }

    @Test
    void validToBeforeValidFromIsRefused() {
        assertThatThrownBy(() -> service.create(pkg(null, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1), true, null,
                List.of(item(PriceListItemType.SERVICE, 1501L, 1)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("Valid To");
    }

    @Test
    void notForAllDepartmentsNeedsAtLeastOneDepartment() {
        assertThatThrownBy(() -> service.create(pkg(null, null, null, false, List.of(),
                List.of(item(PriceListItemType.SERVICE, 1501L, 1)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("department");
    }

    @Test
    void departmentsAreSavedWhenNotForAll() {
        service.create(pkg(null, null, null, false, List.of(5000L, 5000L, 5001L),
                List.of(item(PriceListItemType.SERVICE, 1501L, 1))));

        verify(packageSetupDepartmentRepository).deleteByPackageSetupId(eq(1L));
        verify(packageSetupDepartmentRepository).saveAll(org.mockito.ArgumentMatchers.<List<com.dazzle.asklepios.domain.PackageSetupDepartment>>argThat(
                list -> list.size() == 2)); // the repeated department is saved once
    }

    @Test
    void theSameCategoryTwiceIsRefused() {
        assertThatThrownBy(() -> service.create(pkg(null, null, null, true, null, List.of(
                category(PriceListItemType.LABORATORY, 5, null, null),
                category(PriceListItemType.LABORATORY, 2, null, null)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("same category");
    }

    @Test
    void theSameItemTwiceIsRefused() {
        assertThatThrownBy(() -> service.create(pkg(null, null, null, true, null, List.of(
                item(PriceListItemType.SERVICE, 1501L, 1),
                item(PriceListItemType.SERVICE, 1501L, 2)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("same item");
    }

    @Test
    void itemLineNeedsTheItem() {
        assertThatThrownBy(() -> service.create(pkg(null, null, null, true, null,
                List.of(item(PriceListItemType.SERVICE, null, 1)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("needs a type and an item");
    }

    @Test
    void packageWithOnlyInactiveItemsIsRefused() {
        PackageSetupItemVM off = new PackageSetupItemVM(null, PriceListItemType.SERVICE, 1501L, null, null, 1, false, false, null, null, null);

        assertThatThrownBy(() -> service.create(pkg(null, null, null, true, null, List.of(off))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("active item");
    }

    @Test
    void discountAbove100IsRefused() {
        assertThatThrownBy(() -> service.create(pkg(null, null, null, true, null,
                List.of(category(PriceListItemType.LABORATORY, 5, null, "120")))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("between 0 and 100");
    }

    @Test
    void itemThatDoesNotExistIsRefused() {
        when(packageItemLookupService.findByIds(eq(PriceListItemType.SERVICE), anyCollection())).thenReturn(Map.of());

        assertThatThrownBy(() -> service.create(pkg(null, null, null, true, null,
                List.of(item(PriceListItemType.SERVICE, 999L, 1)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("Item not found");
    }
}
