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

/** Rules of the package setup screen: dates, departments, items, category lines, and one pricing option. */
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

    private static final BigDecimal PRICE = new BigDecimal("300");
    private static final Long LAB_DISCOUNT = 70L;

    private static PackageSetupItemVM item(PriceListItemType type, Long sourceId, int qty) {
        return new PackageSetupItemVM(null, type, sourceId, null, null, qty, true, false, null, null);
    }

    private static PackageSetupItemVM category(PriceListItemType type, int qty, Integer repeat, Long discountId) {
        return new PackageSetupItemVM(null, type, null, null, null, qty, true, true, repeat, discountId);
    }

    private static PackageSetupSaveVM pkg(BigDecimal price, LocalDate from, LocalDate to, Boolean allDepartments,
                                          List<Long> departments, List<PackageSetupItemVM> items) {
        return new PackageSetupSaveVM("PKG-T", "Test", null, price, true, from, to, null, allDepartments, departments, false, items);
    }

    private static PackageSetupSaveVM withPackageDiscount(BigDecimal price, Long discountId, List<PackageSetupItemVM> items) {
        return new PackageSetupSaveVM("PKG-T", "Test", null, price, true, null, null, discountId, true, null, false, items);
    }

    @Test
    void categoryAndItemLinesAreSavedWithTheirRules() {
        PackageSetupResponseVM saved = service.create(pkg(PRICE, null, null, true, null, List.of(
                category(PriceListItemType.LABORATORY, 5, 1, null),
                item(PriceListItemType.SERVICE, 1501L, 2))));

        assertThat(saved.items()).hasSize(2);
        PackageSetupItem anyLab = savedItems.get(0);
        assertThat(anyLab.getIsCategory()).isTrue();
        assertThat(anyLab.getSourceId()).isNull();
        assertThat(anyLab.getMaxRepeat()).isEqualTo(1);
        PackageSetupItem dental = savedItems.get(1);
        assertThat(dental.getItemName()).isEqualTo("Item 1501");
        assertThat(dental.getMaxRepeat()).isNull();
    }

    @Test
    void overrideDefaultServiceIsSaved() {
        PackageSetupResponseVM saved = service.create(new PackageSetupSaveVM("PKG-T", "Test", null, PRICE, true, null, null,
                null, true, null, true, List.of(item(PriceListItemType.SERVICE, 1501L, 1))));

        assertThat(saved.overrideDefaultService()).isTrue();
    }

    @Test
    void overrideDefaultServiceIsOffWhenNotSent() {
        PackageSetupResponseVM saved = service.create(new PackageSetupSaveVM("PKG-T", "Test", null, PRICE, true, null, null,
                null, true, null, null, List.of(item(PriceListItemType.SERVICE, 1501L, 1))));

        assertThat(saved.overrideDefaultService()).isFalse();
    }

    // ---------- one pricing option: fixed price, package discount, or item discounts ----------

    @Test
    void fixedPriceAloneIsAccepted() {
        PackageSetupResponseVM saved = service.create(pkg(PRICE, null, null, true, null,
                List.of(category(PriceListItemType.LABORATORY, 8, 2, null))));

        assertThat(saved.price()).isEqualByComparingTo("300");
    }

    @Test
    void packageDiscountAloneIsAccepted() {
        when(discountRepository.existsById(LAB_DISCOUNT)).thenReturn(true);

        service.create(withPackageDiscount(null, LAB_DISCOUNT, List.of(category(PriceListItemType.LABORATORY, 5, null, null))));

        verify(packageSetupRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(p -> LAB_DISCOUNT.equals(p.getDiscountId())));
    }

    @Test
    void itemDiscountsAloneAreAccepted() {
        when(discountRepository.existsById(LAB_DISCOUNT)).thenReturn(true);

        service.create(pkg(null, null, null, true, null, List.of(
                category(PriceListItemType.LABORATORY, 5, null, LAB_DISCOUNT),
                item(PriceListItemType.SERVICE, 1501L, 1))));

        assertThat(savedItems.get(0).getDiscountId()).isEqualTo(LAB_DISCOUNT);
        assertThat(savedItems.get(1).getDiscountId()).isNull();
    }

    @Test
    void packageWithoutAnyPricingOptionIsRefused() {
        assertThatThrownBy(() -> service.create(pkg(null, null, null, true, null,
                List.of(item(PriceListItemType.SERVICE, 1501L, 1)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("Choose how the package is priced");
        verify(packageSetupRepository, never()).saveAndFlush(any());
    }

    @Test
    void fixedPriceWithPackageDiscountIsRefused() {
        assertThatThrownBy(() -> service.create(withPackageDiscount(PRICE, LAB_DISCOUNT,
                List.of(item(PriceListItemType.SERVICE, 1501L, 1)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("Choose only one");
    }

    @Test
    void fixedPriceWithItemDiscountIsRefused() {
        assertThatThrownBy(() -> service.create(pkg(PRICE, null, null, true, null,
                List.of(category(PriceListItemType.LABORATORY, 5, null, LAB_DISCOUNT)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("Choose only one");
        verify(packageSetupRepository, never()).saveAndFlush(any());
    }

    @Test
    void packageDiscountWithItemDiscountIsRefused() {
        assertThatThrownBy(() -> service.create(withPackageDiscount(null, LAB_DISCOUNT,
                List.of(category(PriceListItemType.LABORATORY, 5, null, LAB_DISCOUNT)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("Choose only one");
    }

    // ---------- the other rules ----------

    @Test
    void validToBeforeValidFromIsRefused() {
        assertThatThrownBy(() -> service.create(pkg(PRICE, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1), true, null,
                List.of(item(PriceListItemType.SERVICE, 1501L, 1)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("Valid To");
    }

    @Test
    void notForAllDepartmentsNeedsAtLeastOneDepartment() {
        assertThatThrownBy(() -> service.create(pkg(PRICE, null, null, false, List.of(),
                List.of(item(PriceListItemType.SERVICE, 1501L, 1)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("department");
    }

    @Test
    void departmentsAreSavedWhenNotForAll() {
        service.create(pkg(PRICE, null, null, false, List.of(5000L, 5000L, 5001L),
                List.of(item(PriceListItemType.SERVICE, 1501L, 1))));

        verify(packageSetupDepartmentRepository).deleteByPackageSetupId(eq(1L));
        verify(packageSetupDepartmentRepository).saveAll(org.mockito.ArgumentMatchers.<List<com.dazzle.asklepios.domain.PackageSetupDepartment>>argThat(
                list -> list.size() == 2)); // the repeated department is saved once
    }

    @Test
    void theSameCategoryTwiceIsRefused() {
        assertThatThrownBy(() -> service.create(pkg(PRICE, null, null, true, null, List.of(
                category(PriceListItemType.LABORATORY, 5, null, null),
                category(PriceListItemType.LABORATORY, 2, null, null)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("same category");
    }

    @Test
    void theSameItemTwiceIsRefused() {
        assertThatThrownBy(() -> service.create(pkg(PRICE, null, null, true, null, List.of(
                item(PriceListItemType.SERVICE, 1501L, 1),
                item(PriceListItemType.SERVICE, 1501L, 2)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("same item");
    }

    @Test
    void itemLineNeedsTheItem() {
        assertThatThrownBy(() -> service.create(pkg(PRICE, null, null, true, null,
                List.of(item(PriceListItemType.SERVICE, null, 1)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("needs a type and an item");
    }

    @Test
    void packageWithOnlyInactiveItemsIsRefused() {
        PackageSetupItemVM off = new PackageSetupItemVM(null, PriceListItemType.SERVICE, 1501L, null, null, 1, false, false, null, null);

        assertThatThrownBy(() -> service.create(pkg(PRICE, null, null, true, null, List.of(off))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("active item");
    }

    @Test
    void itemThatDoesNotExistIsRefused() {
        when(packageItemLookupService.findByIds(eq(PriceListItemType.SERVICE), anyCollection())).thenReturn(Map.of());

        assertThatThrownBy(() -> service.create(pkg(PRICE, null, null, true, null,
                List.of(item(PriceListItemType.SERVICE, 999L, 1)))))
                .isInstanceOf(BadRequestAlertException.class).hasMessageContaining("Item not found");
    }
}
