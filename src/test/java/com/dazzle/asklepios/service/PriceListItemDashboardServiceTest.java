package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.PriceListSetupItem;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PayorRepository;
import com.dazzle.asklepios.repository.PriceListSetupItemRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemDashboardVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PriceListItemDashboardServiceTest {

    @Mock
    private PriceListSetupRepository priceListSetupRepository;

    @Mock
    private PriceListSetupItemRepository priceListSetupItemRepository;

    @Mock
    private PayorRepository payorRepository;

    @Mock
    private NphiesPayerRepository nphiesPayerRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @InjectMocks
    private PriceListItemDashboardService dashboardService;

    private PriceListSetup cashList;
    private PriceListSetup insuranceList;
    private NphiesPayer nphiesPayer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cashList = PriceListSetup.builder()
                .id(1L)
                .facilityId(10L)
                .name("Cash 2026")
                .type(PriceListSetupType.CASH)
                .status(PriceListSetupStatus.ACTIVE)
                .currency(Currency.SAR)
                .isActive(true)
                .appliesToAllFacilities(false)
                .build();
        insuranceList = PriceListSetup.builder()
                .id(2L)
                .facilityId(10L)
                .name("Bupa 2026")
                .type(PriceListSetupType.INSURANCE)
                .status(PriceListSetupStatus.ACTIVE)
                .currency(Currency.SAR)
                .isActive(true)
                .nphiesPayerId(8L)
                .appliesToAllFacilities(false)
                .build();
        nphiesPayer = NphiesPayer.builder()
                .id(8L)
                .nphiesId("INS-008")
                .nameEn("Bupa")
                .isActive(true)
                .build();
    }

    @Test
    void overview_returnsEmptyDashboardWhenFacilityHasNoPriceLists() {
        when(priceListSetupRepository.listAllVisibleToFacility(10L)).thenReturn(List.of());

        PriceListItemDashboardVM dashboard = dashboardService.overview(10L);

        assertThat(dashboard.summary().priceListCount()).isZero();
        assertThat(dashboard.summary().uniqueItemCount()).isZero();
        assertThat(dashboard.priceLists()).isEmpty();
        verify(priceListSetupItemRepository, never()).countByPriceListSetupIdIn(anyCollection());
    }

    @Test
    void overview_usesCountQueriesForSummary() {
        when(priceListSetupRepository.listAllVisibleToFacility(10L)).thenReturn(List.of(cashList, insuranceList));
        when(nphiesPayerRepository.findByIdIn(anyCollection())).thenReturn(List.of(nphiesPayer));
        when(priceListSetupItemRepository.countByPriceListSetupIdIn(anyCollection())).thenReturn(3L);
        when(priceListSetupItemRepository.countByPriceListSetupIdInAndItemType(
                anyCollection(),
                eq(PriceListItemType.SERVICE)
        )).thenReturn(2L);
        when(priceListSetupItemRepository.countByPriceListSetupIdInAndItemType(
                anyCollection(),
                eq(PriceListItemType.LABORATORY)
        )).thenReturn(1L);

        PriceListItemDashboardVM dashboard = dashboardService.overview(10L);

        assertThat(dashboard.summary().priceListCount()).isEqualTo(2);
        assertThat(dashboard.summary().uniqueItemCount()).isEqualTo(3);
        assertThat(dashboard.summary().byItemType()).extracting(PriceListItemDashboardVM.TypeCountVM::itemType)
                .containsExactly(PriceListItemType.LABORATORY, PriceListItemType.SERVICE);
        assertThat(dashboard.priceLists()).extracting(PriceListItemDashboardVM.PriceListColumnVM::name)
                .containsExactly("Bupa 2026", "Cash 2026");
        verify(priceListSetupItemRepository, never()).findAllByPriceListSetupIdIn(anyCollection(), any());
    }

    @Test
    void searchItems_usesPageableNameSearch() {
        PriceListSetupItem labItem = item(
                21L,
                1L,
                PriceListItemType.LABORATORY,
                200L,
                "CBC",
                "Complete Blood Count",
                EncounterType.CLINIC,
                "40.00",
                "0.00",
                true
        );
        when(priceListSetupRepository.listAllVisibleToFacility(10L)).thenReturn(List.of(cashList, insuranceList));
        when(priceListSetupItemRepository.findAllByPriceListSetupIdInAndItemTypeAndItemNameContainingIgnoreCase(
                anyCollection(),
                eq(PriceListItemType.LABORATORY),
                eq("blood"),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(labItem), PageRequest.of(0, 20), 1));
        when(priceListSetupItemRepository.findAllByPriceListSetupIdInAndItemTypeInAndSourceIdIn(
                anyCollection(),
                anyCollection(),
                anyCollection()
        )).thenReturn(List.of(labItem));

        PriceListItemDashboardVM.ItemPageVM page = dashboardService.searchItems(
                10L,
                "blood",
                PriceListItemType.LABORATORY,
                PageRequest.of(0, 20)
        );

        assertThat(page.content()).extracting(PriceListItemDashboardVM.CatalogItemVM::itemCode)
                .containsExactly("CBC");
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.content().getFirst().coverageStatus()).isEqualTo(PriceListItemDashboardService.PARTIAL);
        verify(priceListSetupItemRepository, never()).findAllByPriceListSetupIdIn(anyCollection(), any());
    }

    @Test
    void searchItems_usesPageableCodeSearch() {
        PriceListSetupItem serviceItem = item(
                11L,
                1L,
                PriceListItemType.SERVICE,
                100L,
                "CONS",
                "Consultation",
                EncounterType.CLINIC,
                "80.00",
                "0.00",
                true
        );
        when(priceListSetupRepository.listAllVisibleToFacility(10L)).thenReturn(List.of(cashList));
        when(priceListSetupItemRepository.findAllByPriceListSetupIdInAndItemCodeContainingIgnoreCase(
                anyCollection(),
                eq("CONS"),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(serviceItem), PageRequest.of(0, 20), 1));
        when(priceListSetupItemRepository.findAllByPriceListSetupIdInAndItemTypeInAndSourceIdIn(
                anyCollection(),
                anyCollection(),
                anyCollection()
        )).thenReturn(List.of(serviceItem));

        PriceListItemDashboardVM.ItemPageVM page = dashboardService.searchItems(
                10L,
                "CONS",
                null,
                PageRequest.of(0, 20)
        );

        assertThat(page.content()).extracting(PriceListItemDashboardVM.CatalogItemVM::itemName)
                .containsExactly("Consultation");
        verify(priceListSetupItemRepository).findAllByPriceListSetupIdInAndItemCodeContainingIgnoreCase(
                anyCollection(),
                eq("CONS"),
                any(Pageable.class)
        );
    }

    @Test
    void searchItems_keepsDatabasePageWithoutLoadingAllItems() {
        PriceListSetupItem charlie = item(
                31L,
                1L,
                PriceListItemType.SERVICE,
                3L,
                "C",
                "Charlie",
                EncounterType.CLINIC,
                "30.00",
                "0.00",
                true
        );
        when(priceListSetupRepository.listAllVisibleToFacility(10L)).thenReturn(List.of(cashList));
        when(priceListSetupItemRepository.findAllByPriceListSetupIdIn(anyCollection(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(charlie), PageRequest.of(1, 2), 3));
        when(priceListSetupItemRepository.findAllByPriceListSetupIdInAndItemTypeInAndSourceIdIn(
                anyCollection(),
                anyCollection(),
                anyCollection()
        )).thenReturn(List.of(charlie));

        PriceListItemDashboardVM.ItemPageVM page = dashboardService.searchItems(
                10L,
                null,
                null,
                PageRequest.of(1, 2)
        );

        assertThat(page.page()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(2);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(page.content()).extracting(PriceListItemDashboardVM.CatalogItemVM::itemName)
                .containsExactly("Charlie");
    }

    @Test
    void findItem_returnsEveryFacilityPriceListWithPresenceAndDetails() {
        when(priceListSetupRepository.listAllVisibleToFacility(10L)).thenReturn(List.of(cashList, insuranceList));
        when(nphiesPayerRepository.findByIdIn(anyCollection())).thenReturn(List.of(nphiesPayer));
        when(priceListSetupItemRepository.findAllByPriceListSetupIdInAndItemTypeAndSourceId(
                anyCollection(),
                eq(PriceListItemType.SERVICE),
                eq(100L)
        )).thenReturn(List.of(
                item(11L, 1L, PriceListItemType.SERVICE, 100L, "CONS", "Consultation", EncounterType.CLINIC, "80.00", "10.00", true),
                item(12L, 1L, PriceListItemType.SERVICE, 100L, "CONS", "Consultation", EncounterType.INPATIENT, "120.00", "0.00", true)
        ));

        PriceListItemDashboardVM.ItemCardVM card = dashboardService.findItem(
                10L,
                PriceListItemType.SERVICE,
                100L
        ).orElseThrow();

        assertThat(card.presentInCount()).isEqualTo(1);
        assertThat(card.missingFromCount()).isEqualTo(1);
        assertThat(card.coverageStatus()).isEqualTo(PriceListItemDashboardService.PARTIAL);
        assertThat(card.hasPriceVariance()).isTrue();
        assertThat(card.priceLists()).hasSize(2);

        PriceListItemDashboardVM.PriceListCoverageVM cash = card.priceLists().stream()
                .filter(row -> "Cash 2026".equals(row.priceList().name()))
                .findFirst()
                .orElseThrow();
        assertThat(cash.present()).isTrue();
        assertThat(cash.presence()).isEqualTo(PriceListItemDashboardService.PRESENT);
        assertThat(cash.entries()).extracting(PriceListItemDashboardVM.ItemEntryVM::visitType)
                .containsExactly(EncounterType.CLINIC, EncounterType.INPATIENT);
        assertThat(cash.entries().getFirst().netPrice()).isEqualByComparingTo("72.0000");

        PriceListItemDashboardVM.PriceListCoverageVM insurance = card.priceLists().stream()
                .filter(row -> "Bupa 2026".equals(row.priceList().name()))
                .findFirst()
                .orElseThrow();
        assertThat(insurance.present()).isFalse();
        assertThat(insurance.presence()).isEqualTo(PriceListItemDashboardService.MISSING);
        assertThat(insurance.entries()).isEmpty();
    }

    @Test
    void findItem_returnsEmptyWhenItemIsNotOnAnyFacilityPriceList() {
        when(priceListSetupRepository.listAllVisibleToFacility(10L)).thenReturn(List.of(cashList));
        when(priceListSetupItemRepository.findAllByPriceListSetupIdInAndItemTypeAndSourceId(
                anyCollection(),
                eq(PriceListItemType.SERVICE),
                eq(999L)
        )).thenReturn(List.of());

        Optional<PriceListItemDashboardVM.ItemCardVM> card = dashboardService.findItem(
                10L,
                PriceListItemType.SERVICE,
                999L
        );

        assertThat(card).isEmpty();
        verify(payorRepository, never()).findAllById(any());
    }

    private PriceListSetupItem item(
            Long id,
            Long priceListSetupId,
            PriceListItemType itemType,
            Long sourceId,
            String itemCode,
            String itemName,
            EncounterType visitType,
            String unitPrice,
            String discount,
            boolean active
    ) {
        return PriceListSetupItem.builder()
                .id(id)
                .priceListSetupId(priceListSetupId)
                .itemType(itemType)
                .sourceId(sourceId)
                .itemCode(itemCode)
                .itemName(itemName)
                .visitType(visitType)
                .unitPrice(new BigDecimal(unitPrice))
                .discountPercentage(new BigDecimal(discount))
                .isActive(active)
                .requiresPreAuthorization(false)
                .visitTypeLocked(false)
                .build();
    }
}
