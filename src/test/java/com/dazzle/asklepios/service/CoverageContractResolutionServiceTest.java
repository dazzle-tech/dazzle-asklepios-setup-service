package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageClass;
import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.CoverageCopayment;
import com.dazzle.asklepios.domain.CoverageDiscount;
import com.dazzle.asklepios.domain.CoverageExclusion;
import com.dazzle.asklepios.domain.CoveragePreApproval;
import com.dazzle.asklepios.domain.CoveragePreApprovalItem;
import com.dazzle.asklepios.domain.CoverageTerm;
import com.dazzle.asklepios.domain.CoverageTermItem;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.domain.enumeration.CoverageApprovalScope;
import com.dazzle.asklepios.domain.enumeration.CoverageBasis;
import com.dazzle.asklepios.domain.enumeration.CoverageDiagnosisScope;
import com.dazzle.asklepios.domain.enumeration.CoveragePeriodBasis;
import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.CoverageTermType;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;
import com.dazzle.asklepios.domain.enumeration.patient.YesNoQuestion;
import com.dazzle.asklepios.repository.CoverageClassRepository;
import com.dazzle.asklepios.repository.CoverageContractRepository;
import com.dazzle.asklepios.repository.CoverageCopaymentRepository;
import com.dazzle.asklepios.repository.CoverageDiscountRepository;
import com.dazzle.asklepios.repository.CoverageExclusionRepository;
import com.dazzle.asklepios.repository.CoveragePreApprovalItemRepository;
import com.dazzle.asklepios.repository.CoveragePreApprovalRepository;
import com.dazzle.asklepios.repository.CoverageTermItemRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.repository.CoverageTermRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.repository.TpaDefinitionRepository;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractResolveRequest;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractResolveResponse;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractResponseVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CoverageContractResolutionServiceTest {

    @Mock
    private CoverageClassRepository coverageClassRepository;

    @Mock
    private CoverageContractRepository coverageContractRepository;

    @Mock
    private CoverageCopaymentRepository coverageCopaymentRepository;

    @Mock
    private CoverageTermRepository coverageTermRepository;

    @Mock
    private CoverageTermItemRepository coverageTermItemRepository;

    @Mock
    private CoverageDiscountRepository coverageDiscountRepository;

    @Mock
    private CoverageExclusionRepository coverageExclusionRepository;

    @Mock
    private CoveragePreApprovalRepository coveragePreApprovalRepository;

    @Mock
    private CoveragePreApprovalItemRepository coveragePreApprovalItemRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private NphiesPayerRepository nphiesPayerRepository;

    @Mock
    private TpaDefinitionRepository tpaDefinitionRepository;

    @Mock
    private PriceListSetupRepository priceListSetupRepository;

    @Mock
    private CoverageContractService coverageContractService;

    @Mock
    private CoverageLookupService coverageLookupService;

    @InjectMocks
    private CoverageContractResolutionService coverageContractResolutionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(coverageLookupService.ruleCoversAnyDiagnosis(any(), any())).thenAnswer(invocation -> {
            Long ruleId = invocation.getArgument(0);
            Collection<?> ids = invocation.getArgument(1);
            return ruleId != null && ids != null && ids.contains(ruleId);
        });
        when(coverageClassRepository.findByCoverageContract_IdInAndIsActiveTrue(any())).thenAnswer(invocation -> {
            Collection<?> ids = invocation.getArgument(0);
            return ids.stream().map(id -> CoverageClass.builder()
                    .id((Long) id)
                    .name("A")
                    .isActive(true)
                    .coverageContract(CoverageContract.builder().id((Long) id).build())
                    .build()).toList();
        });
        when(coverageTermRepository.findByCoverageClass_IdAndTermTypeAndIsActiveTrue(any(), any()))
                .thenReturn(List.of());
        when(coverageDiscountRepository.findByCoverageClass_IdAndIsActiveTrue(any())).thenReturn(List.of());
        when(coverageDiscountRepository.findByTpaDefinition_IdAndIsActiveTrue(any())).thenReturn(List.of());
        when(coverageExclusionRepository.findByCoverageClass_IdAndIsActiveTrue(any())).thenReturn(List.of());
        when(coverageExclusionRepository.findByTpaDefinition_IdAndIsActiveTrue(any())).thenReturn(List.of());
        when(coveragePreApprovalRepository.findByCoverageClass_IdAndIsActiveTrue(any())).thenReturn(List.of());
        when(coveragePreApprovalRepository.findByTpaDefinition_IdAndIsActiveTrue(any())).thenReturn(List.of());
        when(coveragePreApprovalItemRepository.findByPreApproval_IdAndIsActiveTrue(any())).thenReturn(List.of());
    }

    @Test
    void resolve_matchesPolicyAndPrefersEncounterCopayment() {
        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        CoverageContract contract = CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.INSURANCE)
                .companyId(9L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .priceListSetupId(4L)
                .isActive(true)
                .build();
        CoverageCopayment clinicCopay = CoverageCopayment.builder()
                .id(31L)
                .coverageClass(CoverageClass.builder().id(contract.getId()).name("A").coverageContract(contract).isActive(true).build())
                .encounterType(EncounterType.CLINIC)
                .valueType(InsuranceCoverageType.PERCENTAGE)
                .valueAmount(new BigDecimal("20"))
                .isActive(true)
                .build();
        CoverageCopayment allCopay = CoverageCopayment.builder()
                .id(32L)
                .coverageClass(CoverageClass.builder().id(contract.getId()).name("A").coverageContract(contract).isActive(true).build())
                .encounterType(EncounterType.ALL)
                .valueType(InsuranceCoverageType.PERCENTAGE)
                .valueAmount(new BigDecimal("10"))
                .isActive(true)
                .build();

        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(priceListSetupRepository.findById(4L)).thenReturn(Optional.of(
                PriceListSetup.builder()
                        .id(4L)
                        .effectiveFrom(LocalDate.now().minusDays(1))
                        .effectiveTo(LocalDate.now().plusDays(1))
                        .build()
        ));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(9L)).thenReturn(List.of());
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of(clinicCopay, allCopay));
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        null,
                        null,
                        "POL-100",
                        "Class A",
                        "CLINIC",
                        LocalDate.now()
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.contract().id()).isEqualTo(21L);
        assertThat(resolved.copayment()).isNotNull();
        assertThat(resolved.copayment().encounterType()).isEqualTo(EncounterType.CLINIC);
        assertThat(resolved.copayment().valueAmount()).isEqualByComparingTo("20");
    }

    @Test
    void resolve_unmatchedWhenPolicyDoesNotExist() {
        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "missing"))
                .thenReturn(List.of());

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(null, "INS-1", null, null, "missing", "A", "CLINIC", null)
        );

        assertThat(resolved.matched()).isFalse();
        assertThat(resolved.contract()).isNull();
        assertThat(resolved.copayment()).isNull();
    }

    @Test
    void resolve_fallsBackToAllEncounterCopayment() {
        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        CoverageContract contract = CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.INSURANCE)
                .companyId(9L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .isActive(true)
                .build();
        CoverageCopayment allCopay = CoverageCopayment.builder()
                .id(32L)
                .coverageClass(CoverageClass.builder().id(contract.getId()).name("A").coverageContract(contract).isActive(true).build())
                .encounterType(EncounterType.ALL)
                .valueType(InsuranceCoverageType.FIXED)
                .valueAmount(new BigDecimal("75"))
                .isActive(true)
                .build();

        when(nphiesPayerRepository.findById(9L)).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(9L)).thenReturn(List.of());
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of(allCopay));
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(9L, null, null, null, "POL-100", "A", "INPATIENT", LocalDate.now())
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.copayment().encounterType()).isEqualTo(EncounterType.ALL);
        assertThat(resolved.copayment().valueType()).isEqualTo(InsuranceCoverageType.FIXED);
    }

    @Test
    void resolve_prefersSpecificCoverageReadingOverCategory() {
        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        CoverageContract contract = CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.INSURANCE)
                .companyId(9L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .isActive(true)
                .build();
        CoverageTerm term = CoverageTerm.builder()
                .id(11L)
                .facilityId(1L)
                .allDepartments(true)
                .diagnosisScope(CoverageDiagnosisScope.ALL_DIAGNOSIS)
                .encounterType(EncounterType.ALL)
                .valueType(InsuranceCoverageType.PERCENTAGE)
                .limitValue(new BigDecimal("50"))
                .isActive(true)
                .build();
        CoverageTermItem category = CoverageTermItem.builder()
                .id(2L)
                .billingItemType(BillingItemTypes.SERVICE)
                .categoryScope(CoverageRuleTarget.CATEGORY)
                .valueType(InsuranceCoverageType.PERCENTAGE)
                .limitValue(new BigDecimal("70"))
                .isActive(true)
                .build();
        CoverageTermItem service = CoverageTermItem.builder()
                .id(3L)
                .billingItemType(BillingItemTypes.SERVICE)
                .serviceId(90L)
                .categoryScope(CoverageRuleTarget.SERVICE)
                .valueType(InsuranceCoverageType.PERCENTAGE)
                .limitValue(new BigDecimal("80"))
                .isActive(true)
                .build();

        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(9L)).thenReturn(List.of());
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of());
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));
        when(coverageTermRepository.findByCoverageClass_IdAndTermTypeAndIsActiveTrue(21L, CoverageTermType.COVERAGE))
                .thenReturn(List.of(term));
        when(coverageTermItemRepository.findByCoverageTerm_IdAndIsActiveTrue(11L))
                .thenReturn(List.of(category, service));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        null,
                        null,
                        "POL-100",
                        "A",
                        "CLINIC",
                        LocalDate.now(),
                        1L,
                        5L,
                        List.of(),
                        "SERVICE",
                        90L
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.coverageConfigured()).isTrue();
        assertThat(resolved.uncovered()).isFalse();
        assertThat(resolved.coverage()).isNotNull();
        assertThat(resolved.coverage().serviceId()).isEqualTo(90L);
        assertThat(resolved.coverage().limitValue()).isEqualByComparingTo("80");
        assertThat(resolved.limit()).isNull();
    }

    @Test
    void resolve_coverageWithoutMatchingReadingUsesRuleLimit() {
        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        CoverageContract contract = CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.INSURANCE)
                .companyId(9L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .isActive(true)
                .build();
        CoverageTerm term = CoverageTerm.builder()
                .id(11L)
                .facilityId(1L)
                .departmentId(5L)
                .allDepartments(false)
                .diagnosisScope(CoverageDiagnosisScope.ALL_DIAGNOSIS)
                .encounterType(EncounterType.ALL)
                .valueType(InsuranceCoverageType.FIXED)
                .limitValue(new BigDecimal("500"))
                .isActive(true)
                .build();
        CoverageTermItem otherService = CoverageTermItem.builder()
                .id(3L)
                .billingItemType(BillingItemTypes.SERVICE)
                .serviceId(28283L)
                .categoryScope(CoverageRuleTarget.SERVICE)
                .valueType(InsuranceCoverageType.FIXED)
                .limitValue(new BigDecimal("10"))
                .isActive(true)
                .build();

        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(9L)).thenReturn(List.of());
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of());
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));
        when(coverageTermRepository.findByCoverageClass_IdAndTermTypeAndIsActiveTrue(21L, CoverageTermType.COVERAGE))
                .thenReturn(List.of(term));
        when(coverageTermItemRepository.findByCoverageTerm_IdAndIsActiveTrue(11L))
                .thenReturn(List.of(otherService));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        null,
                        null,
                        "POL-100",
                        "A",
                        "CLINIC",
                        LocalDate.now(),
                        1L,
                        5L,
                        List.of(),
                        "SERVICE",
                        29272L
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.uncovered()).isFalse();
        assertThat(resolved.coverage()).isNotNull();
        assertThat(resolved.coverage().serviceId()).isNull();
        assertThat(resolved.coverage().limitValue()).isEqualByComparingTo("500");
    }

    @Test
    void resolve_prefersSpecificLimitReadingOverCategory() {
        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        CoverageContract contract = CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.INSURANCE)
                .companyId(9L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .isActive(true)
                .build();
        CoverageTerm term = CoverageTerm.builder()
                .id(41L)
                .facilityId(1L)
                .allDepartments(true)
                .diagnosisScope(CoverageDiagnosisScope.ALL_DIAGNOSIS)
                .encounterType(EncounterType.ALL)
                .periodBasis(CoveragePeriodBasis.PER_ENCOUNTER)
                .coverageBasis(CoverageBasis.NET)
                .valueType(InsuranceCoverageType.FIXED)
                .limitValue(new BigDecimal("500"))
                .isActive(true)
                .build();
        CoverageTermItem category = CoverageTermItem.builder()
                .id(2L)
                .billingItemType(BillingItemTypes.SERVICE)
                .categoryScope(CoverageRuleTarget.CATEGORY)
                .valueType(InsuranceCoverageType.FIXED)
                .limitValue(new BigDecimal("800"))
                .isActive(true)
                .build();
        CoverageTermItem service = CoverageTermItem.builder()
                .id(3L)
                .billingItemType(BillingItemTypes.SERVICE)
                .serviceId(90L)
                .categoryScope(CoverageRuleTarget.SERVICE)
                .valueType(InsuranceCoverageType.FIXED)
                .limitValue(new BigDecimal("300"))
                .isActive(true)
                .build();

        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(9L)).thenReturn(List.of());
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of());
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));
        when(coverageTermRepository.findByCoverageClass_IdAndTermTypeAndIsActiveTrue(21L, CoverageTermType.LIMIT))
                .thenReturn(List.of(term));
        when(coverageTermItemRepository.findByCoverageTerm_IdAndIsActiveTrue(41L))
                .thenReturn(List.of(category, service));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        null,
                        null,
                        "POL-100",
                        "A",
                        "CLINIC",
                        LocalDate.now(),
                        1L,
                        5L,
                        List.of(),
                        "SERVICE",
                        90L
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.uncovered()).isFalse();
        assertThat(resolved.coverage()).isNull();
        assertThat(resolved.limit()).isNotNull();
        assertThat(resolved.limit().serviceId()).isEqualTo(90L);
        assertThat(resolved.limit().limitValue()).isEqualByComparingTo("300");
        assertThat(resolved.limit().periodBasis()).isEqualTo(CoveragePeriodBasis.PER_ENCOUNTER);
        assertThat(resolved.limit().coverageBasis()).isEqualTo(CoverageBasis.NET);
        assertThat(resolved.cashLimit()).isNull();
    }

    @Test
    void resolve_prefersSpecificCashLimitReadingOverCategory() {
        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        CoverageContract contract = CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.INSURANCE)
                .companyId(9L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .isActive(true)
                .build();
        CoverageTerm term = CoverageTerm.builder()
                .id(61L)
                .facilityId(1L)
                .allDepartments(true)
                .diagnosisScope(CoverageDiagnosisScope.ALL_DIAGNOSIS)
                .encounterType(EncounterType.ALL)
                .periodBasis(CoveragePeriodBasis.PER_ENCOUNTER)
                .coverageBasis(CoverageBasis.NET)
                .valueType(InsuranceCoverageType.FIXED)
                .limitValue(new BigDecimal("200"))
                .isActive(true)
                .build();
        CoverageTermItem category = CoverageTermItem.builder()
                .id(2L)
                .billingItemType(BillingItemTypes.SERVICE)
                .categoryScope(CoverageRuleTarget.CATEGORY)
                .valueType(InsuranceCoverageType.FIXED)
                .limitValue(new BigDecimal("400"))
                .isActive(true)
                .build();
        CoverageTermItem service = CoverageTermItem.builder()
                .id(3L)
                .billingItemType(BillingItemTypes.SERVICE)
                .serviceId(90L)
                .categoryScope(CoverageRuleTarget.SERVICE)
                .valueType(InsuranceCoverageType.FIXED)
                .limitValue(new BigDecimal("150"))
                .isActive(true)
                .build();

        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(9L)).thenReturn(List.of());
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of());
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));
        when(coverageTermRepository.findByCoverageClass_IdAndTermTypeAndIsActiveTrue(21L, CoverageTermType.CASH_LIMIT))
                .thenReturn(List.of(term));
        when(coverageTermItemRepository.findByCoverageTerm_IdAndIsActiveTrue(61L))
                .thenReturn(List.of(category, service));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        null,
                        null,
                        "POL-100",
                        "A",
                        "CLINIC",
                        LocalDate.now(),
                        1L,
                        5L,
                        List.of(),
                        "SERVICE",
                        90L
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.uncovered()).isFalse();
        assertThat(resolved.coverage()).isNull();
        assertThat(resolved.limit()).isNull();
        assertThat(resolved.cashLimit()).isNotNull();
        assertThat(resolved.cashLimit().serviceId()).isEqualTo(90L);
        assertThat(resolved.cashLimit().limitValue()).isEqualByComparingTo("150");
        assertThat(resolved.cashLimit().periodBasis()).isEqualTo(CoveragePeriodBasis.PER_ENCOUNTER);
        assertThat(resolved.cashLimit().coverageBasis()).isEqualTo(CoverageBasis.NET);
    }

    @Test
    void resolve_cashLimitWithoutSpecificItemUsesGeneralHeader() {
        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        CoverageContract contract = CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.INSURANCE)
                .companyId(9L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .isActive(true)
                .build();
        CoverageTerm term = CoverageTerm.builder()
                .id(41L)
                .facilityId(1L)
                .allDepartments(true)
                .diagnosisScope(CoverageDiagnosisScope.ALL_DIAGNOSIS)
                .encounterType(EncounterType.ALL)
                .periodBasis(CoveragePeriodBasis.PER_DAY)
                .coverageBasis(CoverageBasis.GROSS)
                .valueType(InsuranceCoverageType.FIXED)
                .limitValue(new BigDecimal("500"))
                .isActive(true)
                .build();
        CoverageTermItem service = CoverageTermItem.builder()
                .id(3L)
                .billingItemType(BillingItemTypes.LABORATORY)
                .serviceId(77L)
                .categoryScope(CoverageRuleTarget.SERVICE)
                .valueType(InsuranceCoverageType.FIXED)
                .limitValue(new BigDecimal("500"))
                .isActive(true)
                .build();

        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(9L)).thenReturn(List.of());
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of());
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));
        when(coverageTermRepository.findByCoverageClass_IdAndTermTypeAndIsActiveTrue(21L, CoverageTermType.CASH_LIMIT))
                .thenReturn(List.of(term));
        when(coverageTermItemRepository.findByCoverageTerm_IdAndIsActiveTrue(41L))
                .thenReturn(List.of(service));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        null,
                        null,
                        "POL-100",
                        "A",
                        "CLINIC",
                        LocalDate.now(),
                        1L,
                        5L,
                        List.of(),
                        "SERVICE",
                        90L
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.uncovered()).isFalse();
        assertThat(resolved.cashLimit()).isNotNull();
        assertThat(resolved.cashLimit().serviceId()).isNull();
        assertThat(resolved.cashLimit().billingItemType()).isNull();
        assertThat(resolved.cashLimit().limitValue()).isEqualByComparingTo("500");
        assertThat(resolved.cashLimit().periodBasis()).isEqualTo(CoveragePeriodBasis.PER_DAY);
        assertThat(resolved.cashLimit().coverageBasis()).isEqualTo(CoverageBasis.GROSS);
    }

    @Test
    void resolve_unmatchedLimitDoesNotMarkItemUncovered() {
        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        CoverageContract contract = CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.INSURANCE)
                .companyId(9L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .isActive(true)
                .build();
        CoverageTerm term = CoverageTerm.builder()
                .id(41L)
                .facilityId(1L)
                .allDepartments(true)
                .diagnosisScope(CoverageDiagnosisScope.ALL_DIAGNOSIS)
                .encounterType(EncounterType.ALL)
                .periodBasis(CoveragePeriodBasis.PER_DAY)
                .coverageBasis(CoverageBasis.GROSS)
                .valueType(InsuranceCoverageType.FIXED)
                .limitValue(new BigDecimal("500"))
                .isActive(true)
                .build();
        CoverageTermItem service = CoverageTermItem.builder()
                .id(3L)
                .billingItemType(BillingItemTypes.LABORATORY)
                .serviceId(77L)
                .categoryScope(CoverageRuleTarget.SERVICE)
                .valueType(InsuranceCoverageType.FIXED)
                .limitValue(new BigDecimal("500"))
                .isActive(true)
                .build();

        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(9L)).thenReturn(List.of());
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of());
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));
        when(coverageTermRepository.findByCoverageClass_IdAndTermTypeAndIsActiveTrue(21L, CoverageTermType.LIMIT))
                .thenReturn(List.of(term));
        when(coverageTermItemRepository.findByCoverageTerm_IdAndIsActiveTrue(41L))
                .thenReturn(List.of(service));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        null,
                        null,
                        "POL-100",
                        "A",
                        "CLINIC",
                        LocalDate.now(),
                        1L,
                        5L,
                        List.of(),
                        "SERVICE",
                        90L
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.uncovered()).isFalse();
        assertThat(resolved.limit()).isNotNull();
        assertThat(resolved.limit().serviceId()).isNull();
        assertThat(resolved.limit().billingItemType()).isNull();
        assertThat(resolved.limit().limitValue()).isEqualByComparingTo("500");
        assertThat(resolved.cashLimit()).isNull();
    }

    @Test
    void resolve_prefersServiceDiscountOverCategory() {
        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        CoverageContract contract = CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.INSURANCE)
                .companyId(9L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .isActive(true)
                .build();
        CoverageDiscount category = CoverageDiscount.builder()
                .id(2L)
                .targetType(CoverageRuleTarget.CATEGORY)
                .billingItemType(BillingItemTypes.SERVICE)
                .encounterType(EncounterType.ALL)
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("5"))
                .isActive(true)
                .build();
        CoverageDiscount service = CoverageDiscount.builder()
                .id(3L)
                .targetType(CoverageRuleTarget.SERVICE)
                .billingItemType(BillingItemTypes.SERVICE)
                .serviceId(90L)
                .encounterType(EncounterType.ALL)
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .isActive(true)
                .build();

        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(9L)).thenReturn(List.of());
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of());
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));
        when(coverageDiscountRepository.findByCoverageClass_IdAndIsActiveTrue(21L))
                .thenReturn(List.of(category, service));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        null,
                        null,
                        "POL-100",
                        "A",
                        "CLINIC",
                        LocalDate.now(),
                        1L,
                        5L,
                        List.of(),
                        "SERVICE",
                        90L
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.discount()).isNotNull();
        assertThat(resolved.discount().serviceId()).isEqualTo(90L);
        assertThat(resolved.discount().discountValue()).isEqualByComparingTo("10");
    }

    @Test
    void resolve_contractDiscountBeatsTpaDiscountOnSameTarget() {
        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        CoverageContract contract = CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.TPA)
                .companyId(3L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .isActive(true)
                .build();
        CoverageDiscount contractDiscount = CoverageDiscount.builder()
                .id(2L)
                .targetType(CoverageRuleTarget.ALL)
                .encounterType(EncounterType.ALL)
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("5"))
                .isActive(true)
                .build();
        CoverageDiscount tpaDiscount = CoverageDiscount.builder()
                .id(8L)
                .targetType(CoverageRuleTarget.ALL)
                .encounterType(EncounterType.ALL)
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("15"))
                .isActive(true)
                .build();

        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(tpaDefinitionRepository.findById(3L)).thenReturn(Optional.of(
                TpaDefinition.builder().id(3L).isActive(true).build()
        ));
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of());
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));
        when(coverageDiscountRepository.findByCoverageClass_IdAndIsActiveTrue(21L))
                .thenReturn(List.of(contractDiscount));
        when(coverageDiscountRepository.findByTpaDefinition_IdAndIsActiveTrue(3L))
                .thenReturn(List.of(tpaDiscount));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        3L,
                        null,
                        "POL-100",
                        "A",
                        "CLINIC",
                        LocalDate.now(),
                        1L,
                        5L,
                        List.of(),
                        "SERVICE",
                        90L
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.discount()).isNotNull();
        assertThat(resolved.discount().discountValue()).isEqualByComparingTo("5");
    }

    @Test
    void resolve_prefersServiceExclusionOverCategory() {
        CoverageContract contract = matchedContract();
        CoverageExclusion category = CoverageExclusion.builder()
                .id(2L)
                .exclusionType(CoverageRuleTarget.CATEGORY)
                .billingItemType(BillingItemTypes.SERVICE)
                .encounterType(EncounterType.ALL)
                .excludedResult(YesNoQuestion.YES)
                .isActive(true)
                .build();
        CoverageExclusion service = CoverageExclusion.builder()
                .id(3L)
                .exclusionType(CoverageRuleTarget.SERVICE)
                .billingItemType(BillingItemTypes.SERVICE)
                .serviceId(90L)
                .encounterType(EncounterType.ALL)
                .excludedResult(YesNoQuestion.NO)
                .isActive(true)
                .build();

        stubMatchedContract(contract);
        when(coverageExclusionRepository.findByCoverageClass_IdAndIsActiveTrue(21L))
                .thenReturn(List.of(category, service));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(clinicServiceRequest());

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.uncovered()).isFalse();
        assertThat(resolved.exclusion()).isNotNull();
        assertThat(resolved.exclusion().serviceId()).isEqualTo(90L);
        assertThat(resolved.exclusion().excludedResult()).isEqualTo(YesNoQuestion.NO);
        assertThat(resolved.matchReason()).contains("Exclusion override keeps the item covered");
    }

    @Test
    void resolve_contractExclusionBeatsTpaExclusionOnSameTarget() {
        CoverageContract contract = CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.TPA)
                .companyId(3L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .isActive(true)
                .build();
        CoverageExclusion contractExclusion = CoverageExclusion.builder()
                .id(2L)
                .exclusionType(CoverageRuleTarget.ALL)
                .encounterType(EncounterType.ALL)
                .excludedResult(YesNoQuestion.NO)
                .isActive(true)
                .build();
        CoverageExclusion tpaExclusion = CoverageExclusion.builder()
                .id(8L)
                .exclusionType(CoverageRuleTarget.ALL)
                .encounterType(EncounterType.ALL)
                .excludedResult(YesNoQuestion.YES)
                .isActive(true)
                .build();

        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(tpaDefinitionRepository.findById(3L)).thenReturn(Optional.of(
                TpaDefinition.builder().id(3L).isActive(true).build()
        ));
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of());
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));
        when(coverageExclusionRepository.findByCoverageClass_IdAndIsActiveTrue(21L))
                .thenReturn(List.of(contractExclusion));
        when(coverageExclusionRepository.findByTpaDefinition_IdAndIsActiveTrue(3L))
                .thenReturn(List.of(tpaExclusion));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        3L,
                        null,
                        "POL-100",
                        "A",
                        "CLINIC",
                        LocalDate.now(),
                        1L,
                        5L,
                        List.of(),
                        "SERVICE",
                        90L
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.exclusion()).isNotNull();
        assertThat(resolved.exclusion().id()).isEqualTo(2L);
        assertThat(resolved.exclusion().excludedResult()).isEqualTo(YesNoQuestion.NO);
    }

    @Test
    void resolve_usesTpaExclusionWhenContractHasNoSameTarget() {
        CoverageContract contract = CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.TPA)
                .companyId(3L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .isActive(true)
                .build();
        CoverageExclusion tpaExclusion = CoverageExclusion.builder()
                .id(8L)
                .exclusionType(CoverageRuleTarget.SERVICE)
                .serviceId(90L)
                .encounterType(EncounterType.ALL)
                .excludedResult(YesNoQuestion.YES)
                .isActive(true)
                .build();

        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(tpaDefinitionRepository.findById(3L)).thenReturn(Optional.of(
                TpaDefinition.builder().id(3L).isActive(true).build()
        ));
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of());
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));
        when(coverageExclusionRepository.findByTpaDefinition_IdAndIsActiveTrue(3L))
                .thenReturn(List.of(tpaExclusion));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        3L,
                        null,
                        "POL-100",
                        "A",
                        "CLINIC",
                        LocalDate.now(),
                        1L,
                        5L,
                        List.of(),
                        "SERVICE",
                        90L
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.exclusion()).isNotNull();
        assertThat(resolved.exclusion().id()).isEqualTo(8L);
        assertThat(resolved.exclusion().excludedResult()).isEqualTo(YesNoQuestion.YES);
        assertThat(resolved.matchReason()).contains("Item is excluded from coverage");
    }

    @Test
    void resolve_unmatchedExclusionDoesNotMarkItemUncovered() {
        CoverageContract contract = matchedContract();
        CoverageExclusion otherService = CoverageExclusion.builder()
                .id(3L)
                .exclusionType(CoverageRuleTarget.SERVICE)
                .serviceId(91L)
                .encounterType(EncounterType.ALL)
                .excludedResult(YesNoQuestion.YES)
                .isActive(true)
                .build();

        stubMatchedContract(contract);
        when(coverageExclusionRepository.findByCoverageClass_IdAndIsActiveTrue(21L))
                .thenReturn(List.of(otherService));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(clinicServiceRequest());

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.uncovered()).isFalse();
        assertThat(resolved.exclusion()).isNull();
        assertThat(resolved.matchReason()).doesNotContain("excluded");
    }

    @Test
    void resolve_prefersSpecificDiagnosisExclusionOverAllDiagnoses() {
        CoverageContract contract = matchedContract();
        CoverageExclusion allDiagnoses = CoverageExclusion.builder()
                .id(2L)
                .exclusionType(CoverageRuleTarget.DIAGNOSIS)
                .allDiagnoses(true)
                .encounterType(EncounterType.ALL)
                .excludedResult(YesNoQuestion.YES)
                .isActive(true)
                .build();
        CoverageExclusion specific = CoverageExclusion.builder()
                .id(3L)
                .exclusionType(CoverageRuleTarget.DIAGNOSIS)
                .diagnosisId(77L)
                .encounterType(EncounterType.ALL)
                .excludedResult(YesNoQuestion.NO)
                .isActive(true)
                .build();

        stubMatchedContract(contract);
        when(coverageExclusionRepository.findByCoverageClass_IdAndIsActiveTrue(21L))
                .thenReturn(List.of(allDiagnoses, specific));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        null,
                        null,
                        "POL-100",
                        "A",
                        "CLINIC",
                        LocalDate.now(),
                        1L,
                        5L,
                        List.of(77L),
                        "SERVICE",
                        90L
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.exclusion()).isNotNull();
        assertThat(resolved.exclusion().diagnosisId()).isEqualTo(77L);
        assertThat(resolved.exclusion().excludedResult()).isEqualTo(YesNoQuestion.NO);
    }

    @Test
    void resolve_matchesChildDiagnosisWhenRuleUsesParentCode() {
        CoverageContract contract = matchedContract();
        CoverageExclusion parentCode = CoverageExclusion.builder()
                .id(4L)
                .exclusionType(CoverageRuleTarget.DIAGNOSIS)
                .diagnosisId(10L)
                .encounterType(EncounterType.ALL)
                .excludedResult(YesNoQuestion.YES)
                .isActive(true)
                .build();

        stubMatchedContract(contract);
        when(coverageExclusionRepository.findByCoverageClass_IdAndIsActiveTrue(21L))
                .thenReturn(List.of(parentCode));
        when(coverageLookupService.ruleCoversAnyDiagnosis(eq(10L), any()))
                .thenReturn(true);

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        null,
                        null,
                        "POL-100",
                        "A",
                        "CLINIC",
                        LocalDate.now(),
                        1L,
                        5L,
                        List.of(11L),
                        "SERVICE",
                        90L
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.exclusion()).isNotNull();
        assertThat(resolved.exclusion().diagnosisId()).isEqualTo(10L);
    }

    @Test
    void resolve_skipsUndeterminedExclusionResult() {
        CoverageContract contract = matchedContract();
        CoverageExclusion undetermined = CoverageExclusion.builder()
                .id(3L)
                .exclusionType(CoverageRuleTarget.ALL)
                .encounterType(EncounterType.ALL)
                .excludedResult(YesNoQuestion.NOT_YET_DETERMINED)
                .isActive(true)
                .build();

        stubMatchedContract(contract);
        when(coverageExclusionRepository.findByCoverageClass_IdAndIsActiveTrue(21L))
                .thenReturn(List.of(undetermined));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(clinicServiceRequest());

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.exclusion()).isNull();
    }

    @Test
    void resolve_prefersServicePreApprovalOverAll() {
        CoverageContract contract = matchedContract();
        CoveragePreApproval header = facilityPreApproval(11L);
        CoveragePreApprovalItem all = CoveragePreApprovalItem.builder()
                .id(2L)
                .preApproval(header)
                .itemType(CoverageRuleTarget.ALL)
                .allDiagnoses(false)
                .isActive(true)
                .build();
        CoveragePreApprovalItem service = CoveragePreApprovalItem.builder()
                .id(3L)
                .preApproval(header)
                .itemType(CoverageRuleTarget.SERVICE)
                .serviceId(90L)
                .allDiagnoses(false)
                .isActive(true)
                .build();

        stubMatchedContract(contract);
        when(coveragePreApprovalRepository.findByCoverageClass_IdAndIsActiveTrue(21L))
                .thenReturn(List.of(header));
        when(coveragePreApprovalItemRepository.findByPreApproval_IdAndIsActiveTrue(11L))
                .thenReturn(List.of(all, service));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(clinicServiceRequest());

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.preApproval()).isNotNull();
        assertThat(resolved.preApproval().itemId()).isEqualTo(3L);
        assertThat(resolved.preApproval().serviceId()).isEqualTo(90L);
        assertThat(resolved.matchReason()).contains("Pre-approval is required");
    }

    @Test
    void resolve_contractPreApprovalBeatsTpaOnSameTarget() {
        CoverageContract contract = CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.TPA)
                .companyId(3L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .isActive(true)
                .build();
        CoveragePreApproval contractHeader = facilityPreApproval(11L);
        CoveragePreApproval tpaHeader = facilityPreApproval(18L);
        CoveragePreApprovalItem contractItem = CoveragePreApprovalItem.builder()
                .id(2L)
                .preApproval(contractHeader)
                .itemType(CoverageRuleTarget.ALL)
                .isActive(true)
                .build();
        CoveragePreApprovalItem tpaItem = CoveragePreApprovalItem.builder()
                .id(8L)
                .preApproval(tpaHeader)
                .itemType(CoverageRuleTarget.ALL)
                .isActive(true)
                .build();

        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(tpaDefinitionRepository.findById(3L)).thenReturn(Optional.of(
                TpaDefinition.builder().id(3L).isActive(true).build()
        ));
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of());
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));
        when(coveragePreApprovalRepository.findByCoverageClass_IdAndIsActiveTrue(21L))
                .thenReturn(List.of(contractHeader));
        when(coveragePreApprovalRepository.findByTpaDefinition_IdAndIsActiveTrue(3L))
                .thenReturn(List.of(tpaHeader));
        when(coveragePreApprovalItemRepository.findByPreApproval_IdAndIsActiveTrue(11L))
                .thenReturn(List.of(contractItem));
        when(coveragePreApprovalItemRepository.findByPreApproval_IdAndIsActiveTrue(18L))
                .thenReturn(List.of(tpaItem));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        3L,
                        null,
                        "POL-100",
                        "A",
                        "CLINIC",
                        LocalDate.now(),
                        1L,
                        5L,
                        List.of(),
                        "SERVICE",
                        90L
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.preApproval()).isNotNull();
        assertThat(resolved.preApproval().preApprovalId()).isEqualTo(11L);
    }

    @Test
    void resolve_usesTpaPreApprovalWhenContractHasNoSameTarget() {
        CoverageContract contract = CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.TPA)
                .companyId(3L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .isActive(true)
                .build();
        CoveragePreApproval tpaHeader = facilityPreApproval(18L);
        CoveragePreApprovalItem tpaItem = CoveragePreApprovalItem.builder()
                .id(8L)
                .preApproval(tpaHeader)
                .itemType(CoverageRuleTarget.SERVICE)
                .serviceId(90L)
                .isActive(true)
                .build();

        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(tpaDefinitionRepository.findById(3L)).thenReturn(Optional.of(
                TpaDefinition.builder().id(3L).isActive(true).build()
        ));
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of());
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));
        when(coveragePreApprovalRepository.findByTpaDefinition_IdAndIsActiveTrue(3L))
                .thenReturn(List.of(tpaHeader));
        when(coveragePreApprovalItemRepository.findByPreApproval_IdAndIsActiveTrue(18L))
                .thenReturn(List.of(tpaItem));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(
                new CoverageContractResolveRequest(
                        null,
                        "INS-1",
                        3L,
                        null,
                        "POL-100",
                        "A",
                        "CLINIC",
                        LocalDate.now(),
                        1L,
                        5L,
                        List.of(),
                        "SERVICE",
                        90L
                )
        );

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.preApproval()).isNotNull();
        assertThat(resolved.preApproval().preApprovalId()).isEqualTo(18L);
    }

    @Test
    void resolve_unmatchedPreApprovalDoesNotRequireApproval() {
        CoverageContract contract = matchedContract();
        CoveragePreApproval header = facilityPreApproval(11L);
        CoveragePreApprovalItem otherService = CoveragePreApprovalItem.builder()
                .id(3L)
                .preApproval(header)
                .itemType(CoverageRuleTarget.SERVICE)
                .serviceId(91L)
                .isActive(true)
                .build();

        stubMatchedContract(contract);
        when(coveragePreApprovalRepository.findByCoverageClass_IdAndIsActiveTrue(21L))
                .thenReturn(List.of(header));
        when(coveragePreApprovalItemRepository.findByPreApproval_IdAndIsActiveTrue(11L))
                .thenReturn(List.of(otherService));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(clinicServiceRequest());

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.preApproval()).isNull();
        assertThat(resolved.matchReason()).doesNotContain("Pre-approval is required");
    }

    @Test
    void resolve_facilityEncounterMismatchSkipsPreApproval() {
        CoverageContract contract = matchedContract();
        CoveragePreApproval header = CoveragePreApproval.builder()
                .id(11L)
                .approvalScope(CoverageApprovalScope.FACILITY)
                .facilityId(1L)
                .encounterType(EncounterType.ER)
                .isActive(true)
                .build();
        CoveragePreApprovalItem all = CoveragePreApprovalItem.builder()
                .id(2L)
                .preApproval(header)
                .itemType(CoverageRuleTarget.ALL)
                .isActive(true)
                .build();

        stubMatchedContract(contract);
        when(coveragePreApprovalRepository.findByCoverageClass_IdAndIsActiveTrue(21L))
                .thenReturn(List.of(header));
        when(coveragePreApprovalItemRepository.findByPreApproval_IdAndIsActiveTrue(11L))
                .thenReturn(List.of(all));

        CoverageContractResolveResponse resolved = coverageContractResolutionService.resolve(clinicServiceRequest());

        assertThat(resolved.matched()).isTrue();
        assertThat(resolved.preApproval()).isNull();
    }

    private CoveragePreApproval facilityPreApproval(Long id) {
        return CoveragePreApproval.builder()
                .id(id)
                .approvalScope(CoverageApprovalScope.FACILITY)
                .facilityId(1L)
                .encounterType(EncounterType.ALL)
                .isActive(true)
                .build();
    }

    private CoverageContract matchedContract() {
        return CoverageContract.builder()
                .id(21L)
                .guarantorType(GuarantorType.INSURANCE)
                .companyId(9L)
                .code("C-1")
                .policyNumber("POL-100")
                .insurancePayerId(9L)
                .priceListSetupId(4L)
                .isActive(true)
                .build();
    }

    private void stubMatchedContract(CoverageContract contract) {
        NphiesPayer payer = NphiesPayer.builder().id(9L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build();
        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1")).thenReturn(Optional.of(payer));
        when(coverageContractRepository.findByIsActiveTrueAndInsurancePayerIdAndPolicyNumberIgnoreCase(9L, "pol-100"))
                .thenReturn(List.of(contract));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(9L)).thenReturn(List.of());
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(21L))
                .thenReturn(List.of());
        when(coverageContractService.toResponse(any(), any())).thenReturn(response(contract));
    }

    private CoverageContractResolveRequest clinicServiceRequest() {
        return new CoverageContractResolveRequest(
                null,
                "INS-1",
                null,
                null,
                "POL-100",
                "A",
                "CLINIC",
                LocalDate.now(),
                1L,
                5L,
                List.of(),
                "SERVICE",
                90L
        );
    }

    private CoverageContractResponseVM response(CoverageContract contract) {
        return new CoverageContractResponseVM(
                contract.getId(),
                contract.getGuarantorType(),
                contract.getCompanyId(),
                "Tawuniya",
                "INS-1",
                contract.getCode(),
                contract.getPolicyNumber(),
                contract.getCoverageBasis(),
                contract.getInsurancePayerId(),
                "Tawuniya",
                contract.getPriceListSetupId(),
                "Price list",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                null,
                null,
                21L,
                "A",
                contract.getApprovalCoverageCompany(),
                true,
                null,
                null,
                null,
                null
        );
    }
}
