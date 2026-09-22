package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import com.dazzle.asklepios.repository.CoverageClassRepository;
import com.dazzle.asklepios.repository.CoverageContractRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.repository.TpaDefinitionRepository;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.PayerRelationshipDashboardVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PayerRelationshipDashboardServiceTest {

    @Mock
    private NphiesPayerRepository nphiesPayerRepository;

    @Mock
    private TpaDefinitionRepository tpaDefinitionRepository;

    @Mock
    private CoverageClassRepository coverageClassRepository;

    @Mock
    private CoverageContractRepository coverageContractRepository;

    @Mock
    private PriceListSetupRepository priceListSetupRepository;

    @InjectMocks
    private PayerRelationshipDashboardService dashboardService;

    private NphiesPayer child;
    private NphiesPayer parent;
    private TpaDefinition tpa;
    private PriceListSetup priceList;
    private CoverageContract insuranceContract;
    private CoverageContract tpaContract;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        child = NphiesPayer.builder()
                .id(8L)
                .nphiesId("INS-008")
                .nameEn("Bupa")
                .isActive(true)
                .parentCompanies(new HashSet<>())
                .childCompanies(new HashSet<>())
                .tpas(new HashSet<>())
                .build();
        parent = NphiesPayer.builder()
                .id(5L)
                .nphiesId("INS-005")
                .nameEn("Tawuniya")
                .isActive(true)
                .parentCompanies(new HashSet<>())
                .childCompanies(new HashSet<>(Set.of(child)))
                .tpas(new HashSet<>())
                .build();
        child.getParentCompanies().add(parent);
        tpa = TpaDefinition.builder()
                .id(9L)
                .tpaCode("TPA-1")
                .name("Medgulf TPA")
                .isActive(true)
                .insuranceCompanies(new HashSet<>(Set.of(child)))
                .build();
        priceList = PriceListSetup.builder()
                .id(44L)
                .name("Bupa 2026")
                .nphiesPayerId(8L)
                .type(PriceListSetupType.INSURANCE)
                .status(PriceListSetupStatus.ACTIVE)
                .isActive(true)
                .build();
        insuranceContract = CoverageContract.builder()
                .id(70L)
                .guarantorType(GuarantorType.INSURANCE)
                .companyId(8L)
                .code("C-1")
                .policyNumber("P-1")
                .insurancePayerId(8L)
                .priceListSetupId(44L)
                .isActive(true)
                .build();
        tpaContract = CoverageContract.builder()
                .id(71L)
                .guarantorType(GuarantorType.TPA)
                .companyId(9L)
                .code("C-2")
                .policyNumber("P-2")
                .insurancePayerId(8L)
                .priceListSetupId(44L)
                .isActive(true)
                .build();
    }

    @Test
    void build_returnsLightListAndSummary() {
        when(nphiesPayerRepository.findAll()).thenReturn(List.of(parent, child));
        when(nphiesPayerRepository.findDistinctByIdIn(anyCollection())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return List.of(parent, child).stream().filter(payer -> ids.contains(payer.getId())).toList();
        });
        when(tpaDefinitionRepository.findAll()).thenReturn(List.of(tpa));
        when(coverageContractRepository.findAll()).thenReturn(List.of(insuranceContract, tpaContract));
        when(priceListSetupRepository.findAll()).thenReturn(List.of(priceList));

        PayerRelationshipDashboardVM dashboard = dashboardService.build(null);

        assertThat(dashboard.summary().insuranceCount()).isEqualTo(2);
        assertThat(dashboard.summary().parentCompanyCount()).isEqualTo(1);
        assertThat(dashboard.summary().childCompanyCount()).isEqualTo(1);
        assertThat(dashboard.insurances())
                .extracting(PayerRelationshipDashboardVM.InsuranceListItemVM::role)
                .containsExactlyInAnyOrder("PARENT", "CHILD");
        assertThat(dashboard.tpas()).extracting(PayerRelationshipDashboardVM.TpaListItemVM::tpaCode)
                .containsExactly("TPA-1");
    }

    @Test
    void build_searchUsesDerivedLookupsWithoutLoadingContracts() {
        when(nphiesPayerRepository.findByNphiesIdContainingIgnoreCaseOrNameEnContainingIgnoreCaseOrNameArContainingIgnoreCase(
                "Bupa",
                "Bupa",
                "Bupa"
        )).thenReturn(List.of(child));
        when(nphiesPayerRepository.findDistinctByIdIn(anyCollection())).thenReturn(List.of(child));
        when(nphiesPayerRepository.findByChildCompanies_IdIn(anyCollection())).thenReturn(List.of(parent));
        when(tpaDefinitionRepository.findByTpaCodeContainingIgnoreCaseOrNameContainingIgnoreCase("Bupa", "Bupa"))
                .thenReturn(List.of());

        PayerRelationshipDashboardVM dashboard = dashboardService.build("Bupa");

        assertThat(dashboard.summary()).isNull();
        assertThat(dashboard.insurances()).extracting(PayerRelationshipDashboardVM.InsuranceListItemVM::nameEn)
                .containsExactly("Bupa");
        assertThat(dashboard.insurances().get(0).role()).isEqualTo("CHILD");
        verify(coverageContractRepository, never()).findAll();
        verify(priceListSetupRepository, never()).findAll();
    }

    @Test
    void findInsurance_loadsDetailsForOnePayer() {
        when(nphiesPayerRepository.findById(8L)).thenReturn(Optional.of(child));
        when(nphiesPayerRepository.findDistinctByIdIn(anyCollection())).thenReturn(List.of(child));
        when(nphiesPayerRepository.findByChildCompanies_IdIn(anyCollection())).thenReturn(List.of(parent));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(8L)).thenReturn(List.of(tpa));
        when(coverageContractRepository.findByInsurancePayerId(8L)).thenReturn(List.of(insuranceContract));
        when(coverageClassRepository.findByCoverageContract_IdIn(anyCollection())).thenReturn(List.of());
        when(priceListSetupRepository.findByNphiesPayerId(8L)).thenReturn(List.of(priceList));
        when(priceListSetupRepository.findByPayerId(8L)).thenReturn(List.of());

        PayerRelationshipDashboardVM.InsuranceCardVM card = dashboardService.findInsurance(8L).orElseThrow();

        assertThat(card.role()).isEqualTo("CHILD");
        assertThat(card.parent().name()).isEqualTo("Tawuniya");
        assertThat(card.tpas()).extracting(PayerRelationshipDashboardVM.LinkedPartyVM::id).containsExactly(9L);
        assertThat(card.priceLists()).extracting(PayerRelationshipDashboardVM.PriceListItemVM::name)
                .containsExactly("Bupa 2026");
        assertThat(card.contracts()).isNotEmpty();
    }
}
