package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageClass;
import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.CoverageCopayment;
import com.dazzle.asklepios.domain.InsuranceCompanyPolicy;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;
import com.dazzle.asklepios.repository.CoverageClassRepository;
import com.dazzle.asklepios.repository.CoverageContractRepository;
import com.dazzle.asklepios.repository.CoverageCopaymentRepository;
import com.dazzle.asklepios.repository.CoverageDiscountRepository;
import com.dazzle.asklepios.repository.CoverageExclusionRepository;
import com.dazzle.asklepios.repository.CoveragePreApprovalItemRepository;
import com.dazzle.asklepios.repository.CoveragePreApprovalRepository;
import com.dazzle.asklepios.repository.CoverageTermExclusionRepository;
import com.dazzle.asklepios.repository.CoverageTermItemRepository;
import com.dazzle.asklepios.repository.CoverageTermRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.repository.TpaDefinitionRepository;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractResolveRequest;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractResolveResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Patient co-payment by facility and department: the visit takes the most specific active rule. */
class CoverageCopaymentSelectionTest {

    private static final Long FACILITY = 1L;
    private static final Long OTHER_FACILITY = 2L;
    private static final Long FAMILY_MEDICINE = 10L;
    private static final Long INTERNAL_MEDICINE = 20L;
    private static final Long DENTAL = 30L;
    private static final Long CLASS_ID = 21L;

    @Mock private CoverageClassRepository coverageClassRepository;
    @Mock private CoverageContractRepository coverageContractRepository;
    @Mock private CoverageCopaymentRepository coverageCopaymentRepository;
    @Mock private CoverageTermRepository coverageTermRepository;
    @Mock private CoverageTermItemRepository coverageTermItemRepository;
    @Mock private CoverageDiscountRepository coverageDiscountRepository;
    @Mock private CoverageExclusionRepository coverageExclusionRepository;
    @Mock private CoveragePreApprovalRepository coveragePreApprovalRepository;
    @Mock private CoveragePreApprovalItemRepository coveragePreApprovalItemRepository;
    @Mock private NphiesPayerRepository nphiesPayerRepository;
    @Mock private TpaDefinitionRepository tpaDefinitionRepository;
    @Mock private PriceListSetupRepository priceListSetupRepository;
    @Mock private CoverageContractService coverageContractService;
    @Mock private CoverageLookupService coverageLookupService;
    @Mock private CatalogSubcategoryLookup catalogSubcategoryLookup;
    @Mock private CoverageTermExclusionRepository coverageTermExclusionRepository;

    @InjectMocks
    private CoverageContractResolutionService coverageContractResolutionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        CoverageContract contract = CoverageContract.builder()
                .id(CLASS_ID)
                .guarantorType(GuarantorType.INSURANCE)
                .companyId(9L)
                .insurancePayerId(9L)
                .priceListSetupId(4L)
                .isActive(true)
                .build();
        CoverageClass coverageClass = CoverageClass.builder()
                .id(CLASS_ID)
                .name("VIPP-10")
                .isActive(true)
                .coverageContract(contract)
                .insuranceCompanyPolicy(InsuranceCompanyPolicy.builder()
                        .id(1L)
                        .policyNumber("1234567890")
                        .policyCode("VIPP-10")
                        .build())
                .build();

        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase("ins-1"))
                .thenReturn(Optional.of(NphiesPayer.builder().id(9L).nphiesId("INS-1").isActive(true).build()));
        when(coverageClassRepository
                .findByIsActiveTrueAndCoverageContract_IsActiveTrueAndCoverageContract_InsurancePayerIdAndInsuranceCompanyPolicy_PolicyNumberIgnoreCase(9L, "1234567890"))
                .thenReturn(List.of(coverageClass));
        when(coverageClassRepository.findByCoverageContract_IdInAndIsActiveTrue(any()))
                .thenReturn(List.of(coverageClass));
        when(priceListSetupRepository.findById(4L)).thenReturn(Optional.of(PriceListSetup.builder()
                .id(4L)
                .effectiveFrom(LocalDate.now().minusDays(1))
                .effectiveTo(LocalDate.now().plusDays(1))
                .build()));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(9L)).thenReturn(List.of());
        when(coverageTermRepository.findByCoverageClass_IdAndTermTypeAndIsActiveTrue(any(), any())).thenReturn(List.of());
        when(coverageDiscountRepository.findByCoverageClass_IdAndIsActiveTrue(any())).thenReturn(List.of());
        when(coverageDiscountRepository.findByTpaDefinition_IdAndIsActiveTrue(any())).thenReturn(List.of());
        when(coverageExclusionRepository.findByCoverageClass_IdAndIsActiveTrue(any())).thenReturn(List.of());
        when(coverageExclusionRepository.findByTpaDefinition_IdAndIsActiveTrue(any())).thenReturn(List.of());
        when(coveragePreApprovalRepository.findByCoverageClass_IdAndIsActiveTrue(any())).thenReturn(List.of());
        when(coveragePreApprovalRepository.findByTpaDefinition_IdAndIsActiveTrue(any())).thenReturn(List.of());
    }

    @Test
    void visitTakesTheRuleOfItsDepartment() {
        givenRules(rule(FACILITY, FAMILY_MEDICINE, EncounterType.ALL, "30"), rule(FACILITY, INTERNAL_MEDICINE, EncounterType.ALL, "50"));

        assertThat(copaymentFor(FACILITY, INTERNAL_MEDICINE, "CLINIC")).isEqualByComparingTo("50");
        assertThat(copaymentFor(FACILITY, FAMILY_MEDICINE, "CLINIC")).isEqualByComparingTo("30");
    }

    @Test
    void departmentRuleBeatsAllDepartmentsRule() {
        givenRules(rule(FACILITY, null, EncounterType.ALL, "10"), rule(FACILITY, FAMILY_MEDICINE, EncounterType.ALL, "30"));

        assertThat(copaymentFor(FACILITY, FAMILY_MEDICINE, "CLINIC")).isEqualByComparingTo("30");
    }

    @Test
    void departmentRuleBeatsAllDepartmentsRuleWithExactEncounterType() {
        givenRules(rule(FACILITY, null, EncounterType.CLINIC, "10"), rule(FACILITY, FAMILY_MEDICINE, EncounterType.ALL, "30"));

        assertThat(copaymentFor(FACILITY, FAMILY_MEDICINE, "CLINIC")).isEqualByComparingTo("30");
    }

    @Test
    void otherDepartmentsFallBackToTheAllDepartmentsRule() {
        givenRules(rule(FACILITY, null, EncounterType.ALL, "10"), rule(FACILITY, FAMILY_MEDICINE, EncounterType.ALL, "30"));

        assertThat(copaymentFor(FACILITY, DENTAL, "CLINIC")).isEqualByComparingTo("10");
    }

    @Test
    void ruleOfAnotherFacilityDoesNotApply() {
        givenRules(rule(OTHER_FACILITY, null, EncounterType.ALL, "10"));

        assertThat(resolve(FACILITY, DENTAL, "CLINIC").copayment()).isNull();
    }

    @Test
    void requestWithoutVisitContextCannotMatchADepartmentRule() {
        givenRules(rule(FACILITY, INTERNAL_MEDICINE, EncounterType.ALL, "50"));

        assertThat(resolve(null, null, "CLINIC").copayment()).isNull();
    }

    @Test
    void resolveRequestReadsFacilityAndDepartmentFromJson() throws Exception {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

        CoverageContractResolveRequest request = mapper.readValue(
                "{\"payerNphiesId\":\"INS-1\",\"policyNumber\":\"1234567890\",\"facilityId\":1,\"departmentId\":20}",
                CoverageContractResolveRequest.class
        );

        assertThat(request.facilityId()).isEqualTo(1L);
        assertThat(request.departmentId()).isEqualTo(20L);
    }

    private void givenRules(CoverageCopayment... rules) {
        when(coverageCopaymentRepository.findByCoverageClass_IdAndIsActiveTrueOrderByLastModifiedDateDesc(CLASS_ID))
                .thenReturn(List.of(rules));
    }

    private CoverageCopayment rule(Long facilityId, Long departmentId, EncounterType encounterType, String value) {
        return CoverageCopayment.builder()
                .facilityId(facilityId)
                .allDepartments(departmentId == null)
                .departmentId(departmentId)
                .encounterType(encounterType)
                .valueType(InsuranceCoverageType.PERCENTAGE)
                .valueAmount(new BigDecimal(value))
                .isActive(true)
                .build();
    }

    private BigDecimal copaymentFor(Long facilityId, Long departmentId, String encounterType) {
        return resolve(facilityId, departmentId, encounterType).copayment().valueAmount();
    }

    private CoverageContractResolveResponse resolve(Long facilityId, Long departmentId, String encounterType) {
        return coverageContractResolutionService.resolve(new CoverageContractResolveRequest(
                null,
                "INS-1",
                null,
                null,
                "1234567890",
                "VIPP-10",
                encounterType,
                LocalDate.now(),
                facilityId,
                departmentId,
                List.of(),
                null,
                null
        ));
    }
}
