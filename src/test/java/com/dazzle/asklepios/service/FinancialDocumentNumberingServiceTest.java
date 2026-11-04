package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.FinancialDocumentNumbering;
import com.dazzle.asklepios.domain.enumeration.BillingConfigurationStatus;
import com.dazzle.asklepios.domain.enumeration.BillingResetFrequency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.dazzle.asklepios.domain.enumeration.biling.FinancialDocumentType;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.FinancialDocumentNumberingRepository;
import com.dazzle.asklepios.service.dto.FinancialDocumentNumberRequest;
import com.dazzle.asklepios.service.dto.FinancialDocumentNumberResponse;
import com.dazzle.asklepios.service.dto.FinancialDocumentNumberingDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FinancialDocumentNumberingServiceTest {

    @Mock
    private FinancialDocumentNumberingRepository numberingRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @InjectMocks
    private FinancialDocumentNumberingService service;

    private FinancialDocumentNumbering numbering;
    private Facility facility;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        facility = Facility.builder()
                .id(1L)
                .name("Main Hospital")
                .code("MH01")
                .type(FacilityType.HOSPITAL)
                .build();

        numbering = FinancialDocumentNumbering.builder()
                .id(10L)
                .facilityId(1L)
                .documentType(FinancialDocumentType.INVOICE)
                .prefix("INV")
                .sequenceLength(6)
                .includeYear(true)
                .includeFacilityCode(false)
                .numberSeparator("-")
                .resetFrequency(BillingResetFrequency.YEARLY)
                .startingNumber(1L)
                .active(true)
                .status(BillingConfigurationStatus.ACTIVE)
                .build();
    }

    @Test
    void generateNextNumber_shouldReturnYearlySequentialInvoiceNumber() {
        when(numberingRepository.findForUpdate(1L, FinancialDocumentType.INVOICE))
                .thenReturn(Optional.of(numbering));

        when(numberingRepository.saveAndFlush(any(FinancialDocumentNumbering.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(facilityRepository.findById(1L))
                .thenReturn(Optional.of(facility));

        FinancialDocumentNumberResponse response = service.generateNextNumber(
                new FinancialDocumentNumberRequest(
                        1L,
                        FinancialDocumentType.INVOICE,
                        LocalDate.of(2026, 7, 25),
                        null
                )
        );

        assertThat(response.documentNumber()).isEqualTo("INV-2026-000001");
        assertThat(response.sequenceNumber()).isEqualTo(1L);
        assertThat(response.periodKey()).isEqualTo("2026");
        assertThat(numbering.getLastNumber()).isEqualTo(1L);
        assertThat(numbering.getCurrentPeriodKey()).isEqualTo("2026");
    }

    @Test
    void generateNextNumber_shouldResetToStartingNumberWhenPeriodChanges() {
        numbering.setCurrentPeriodKey("2025");
        numbering.setLastNumber(99L);

        when(numberingRepository.findForUpdate(1L, FinancialDocumentType.INVOICE))
                .thenReturn(Optional.of(numbering));

        when(numberingRepository.saveAndFlush(any(FinancialDocumentNumbering.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FinancialDocumentNumberResponse response = service.generateNextNumber(
                new FinancialDocumentNumberRequest(
                        1L,
                        FinancialDocumentType.INVOICE,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        assertThat(response.sequenceNumber()).isEqualTo(1L);
        assertThat(response.documentNumber()).isEqualTo("INV-2026-000001");
    }

    @Test
    void generateNextNumber_shouldAdvanceCounterWhenMinimumUsedSequenceIsAhead() {
        numbering.setCurrentPeriodKey("2026");
        numbering.setLastNumber(1L);

        when(numberingRepository.findForUpdate(1L, FinancialDocumentType.DEBIT_NOTE))
                .thenReturn(Optional.of(numbering));

        when(numberingRepository.saveAndFlush(any(FinancialDocumentNumbering.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        numbering.setDocumentType(FinancialDocumentType.DEBIT_NOTE);
        numbering.setPrefix("DNS");

        FinancialDocumentNumberResponse response = service.generateNextNumber(
                new FinancialDocumentNumberRequest(
                        1L,
                        FinancialDocumentType.DEBIT_NOTE,
                        LocalDate.of(2026, 8, 7),
                        3L
                )
        );

        assertThat(response.sequenceNumber()).isEqualTo(4L);
        assertThat(response.documentNumber()).isEqualTo("DNS-2026-000004");
        assertThat(numbering.getLastNumber()).isEqualTo(4L);
    }

    @Test
    void previewDocumentNumber_shouldIncludeFacilityCodeWhenEnabled() {
        numbering.setIncludeFacilityCode(true);

        when(facilityRepository.findById(1L))
                .thenReturn(Optional.of(facility));

        String preview = service.previewDocumentNumber(
                numbering,
                42L,
                LocalDate.of(2026, 1, 1)
        );

        assertThat(preview).isEqualTo("INV-MH01-2026-000042");
    }

    @Test
    void toDto_shouldMapEntityFields() {
        FinancialDocumentNumberingDTO dto = service.toDto(numbering);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.documentType()).isEqualTo(FinancialDocumentType.INVOICE);
        assertThat(dto.prefix()).isEqualTo("INV");
        assertThat(dto.resetFrequency()).isEqualTo(BillingResetFrequency.YEARLY);
    }
}
