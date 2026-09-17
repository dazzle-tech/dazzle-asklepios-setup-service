package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PayorRepository;
import com.dazzle.asklepios.repository.PriceListSetupItemRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.repository.TaxRepository;
import com.dazzle.asklepios.service.dto.PriceListSetupDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PriceListSetupServiceTest {

    @Mock
    private PriceListSetupRepository priceListSetupRepository;

    @Mock
    private PriceListSetupItemRepository priceListSetupItemRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private PayorRepository payorRepository;

    @Mock
    private NphiesPayerRepository nphiesPayerRepository;

    @Mock
    private TaxRepository taxRepository;

    @InjectMocks
    private PriceListSetupService priceListSetupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(priceListSetupRepository.findMaxVersionNumber(any(), any(), any(), any()))
                .thenReturn(0);
        when(facilityRepository.findById(any())).thenReturn(Optional.empty());
        when(payorRepository.findById(any())).thenReturn(Optional.empty());
        when(nphiesPayerRepository.findById(any())).thenReturn(Optional.empty());
        when(taxRepository.findById(any())).thenReturn(Optional.empty());
        when(priceListSetupRepository.save(any(PriceListSetup.class)))
                .thenAnswer(invocation -> {
                    PriceListSetup entity = invocation.getArgument(0);
                    entity.setId(10L);
                    return entity;
                });
    }

    @Test
    void updateAllowsPastStartDateAndPersistsEdits() {
        PriceListSetup existing = existingCashList();
        existing.setEffectiveFrom(LocalDate.now().minusDays(30));
        existing.setName("Old name");
        when(priceListSetupRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(priceListSetupRepository.findAllByFacilityIdAndTypeInAndIsActiveTrue(any(), any()))
                .thenReturn(List.of());
        when(priceListSetupRepository.findAllByAppliesToAllFacilitiesTrueAndTypeInAndIsActiveTrue(any()))
                .thenReturn(List.of());

        PriceListSetupDTO dto = cashDto(LocalDate.now().minusDays(30), PriceListSetupStatus.ACTIVE);
        dto = new PriceListSetupDTO(
                5L,
                dto.facilityId(),
                dto.facilityName(),
                dto.appliesToAllFacilities(),
                dto.type(),
                dto.payerId(),
                dto.payerName(),
                dto.nphiesPayerId(),
                dto.nphiesPayerName(),
                "Updated Cash",
                "UC",
                dto.description(),
                dto.versionNumber(),
                dto.effectiveFrom(),
                dto.effectiveTo(),
                dto.currency(),
                dto.status(),
                dto.taxId(),
                dto.taxName(),
                dto.createdDate(),
                dto.lastModifiedDate(),
                dto.createdBy(),
                dto.lastModifiedBy()
        );

        PriceListSetupDTO result = priceListSetupService.update(5L, dto);

        ArgumentCaptor<PriceListSetup> captor = ArgumentCaptor.forClass(PriceListSetup.class);
        verify(priceListSetupRepository).save(captor.capture());

        PriceListSetup saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Updated Cash");
        assertThat(saved.getShortName()).isEqualTo("UC");
        assertThat(saved.getEffectiveFrom()).isEqualTo(LocalDate.now().minusDays(30));
        assertThat(result.name()).isEqualTo("Updated Cash");
    }

    @Test
    void createRejectsPastStartDate() {
        PriceListSetupDTO dto = cashDto(LocalDate.now().minusDays(1), PriceListSetupStatus.ACTIVE);

        BadRequestAlertException exception = assertThrows(
                BadRequestAlertException.class,
                () -> priceListSetupService.create(dto)
        );

        assertThat(exception.getErrorKey()).isEqualTo("effectiveDate.startInPast");
    }

    @Test
    void createRejectsSecondActiveCashList() {
        when(priceListSetupRepository.findAllByFacilityIdAndTypeInAndIsActiveTrue(any(), any()))
                .thenReturn(List.of(existingCashList()));
        when(priceListSetupRepository.findAllByAppliesToAllFacilitiesTrueAndTypeInAndIsActiveTrue(any()))
                .thenReturn(List.of());

        PriceListSetupDTO dto = cashDto(LocalDate.now().plusMonths(8), PriceListSetupStatus.ACTIVE);

        BadRequestAlertException exception = assertThrows(
                BadRequestAlertException.class,
                () -> priceListSetupService.create(dto)
        );

        assertThat(exception.getErrorKey()).isEqualTo("active.cash.duplicate");
    }

    @Test
    void cloneCreatesInactiveListWithoutStartDate() {
        PriceListSetup source = existingCashList();
        when(priceListSetupRepository.findById(5L)).thenReturn(Optional.of(source));
        when(priceListSetupItemRepository.findAllByPriceListSetupId(5L)).thenReturn(List.of());

        var result = priceListSetupService.clonePriceList(5L, null);

        ArgumentCaptor<PriceListSetup> captor = ArgumentCaptor.forClass(PriceListSetup.class);
        verify(priceListSetupRepository).save(captor.capture());

        PriceListSetup cloned = captor.getValue();
        assertThat(cloned.getEffectiveFrom()).isNull();
        assertThat(cloned.getStatus()).isEqualTo(PriceListSetupStatus.INACTIVE);
        assertThat(result.status()).isEqualTo(PriceListSetupStatus.INACTIVE);
    }

    private PriceListSetupDTO cashDto(LocalDate start, PriceListSetupStatus status) {
        return new PriceListSetupDTO(
                null,
                1L,
                null,
                false,
                PriceListSetupType.CASH,
                null,
                null,
                null,
                null,
                "Cash List",
                null,
                null,
                1,
                start,
                start.plusMonths(12),
                Currency.SAR,
                status,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private PriceListSetup existingCashList() {
        PriceListSetup entity = new PriceListSetup();
        entity.setId(5L);
        entity.setFacilityId(1L);
        entity.setAppliesToAllFacilities(false);
        entity.setType(PriceListSetupType.CASH);
        entity.setName("Existing Cash");
        entity.setVersionNumber(1);
        entity.setEffectiveFrom(LocalDate.now());
        entity.setEffectiveTo(LocalDate.now().plusMonths(6));
        entity.setCurrency(Currency.SAR);
        entity.setStatus(PriceListSetupStatus.ACTIVE);
        entity.setIsActive(true);
        return entity;
    }
}
