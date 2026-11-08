package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageClass;
import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.Payor;
import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import com.dazzle.asklepios.repository.CoverageClassRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PayorRepository;
import com.dazzle.asklepios.repository.TpaDefinitionRepository;
import com.dazzle.asklepios.web.rest.vm.tpadefinition.TpaResolutionVM;
import com.dazzle.asklepios.web.rest.vm.tpadefinition.TpaResolutionVM.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TpaResolutionServiceTest {

    private static final String AL_SAGR = "7001544472";
    private static final String POLICY = "48095070";

    private final TpaDefinition totalcare = tpa(1L, "Totalcare", "7001599658");
    private final TpaDefinition nextcare = tpa(2L, "Nextcare", "7001580856");

    @Mock
    private TpaDefinitionRepository tpaDefinitionRepository;

    @Mock
    private NphiesPayerRepository nphiesPayerRepository;

    @Mock
    private PayorRepository payorRepository;

    @Mock
    private CoverageClassRepository coverageClassRepository;

    @InjectMocks
    private TpaResolutionService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        NphiesPayer payer = NphiesPayer.builder().id(10L).nphiesId(AL_SAGR).build();
        when(nphiesPayerRepository.findFirstByNphiesIdIgnoreCase(AL_SAGR)).thenReturn(Optional.of(payer));
        when(payorRepository.findFirstByNphiesIdIgnoreCase(AL_SAGR)).thenReturn(Optional.empty());
        when(tpaDefinitionRepository.findFirstByNphiesIdIgnoreCase("7001599658")).thenReturn(Optional.of(totalcare));
    }

    @Test
    void payerWithoutTpa_goesDirect() {
        linkTpas();

        assertThat(service.resolve(AL_SAGR, POLICY, null).status()).isEqualTo(Status.NO_TPA);
    }

    @Test
    void payerWithoutLinks_keepsTheTpaEnteredOnThePayor() {
        linkTpas();
        Payor payor = payor("7001599658");
        when(payorRepository.findFirstByNphiesIdIgnoreCase(AL_SAGR)).thenReturn(Optional.of(payor));

        TpaResolutionVM result = service.resolve(AL_SAGR, POLICY, null);

        assertThat(result.status()).isEqualTo(Status.RESOLVED);
        assertThat(result.tpaNphiesId()).isEqualTo("7001599658");
        assertThat(result.tpaName()).isEqualTo("Totalcare");
    }

    @Test
    void noTpaMarkerOnThePayor_meansDirect() {
        linkTpas();
        Payor payor = payor("-1");
        when(payorRepository.findFirstByNphiesIdIgnoreCase(AL_SAGR)).thenReturn(Optional.of(payor));

        assertThat(service.resolve(AL_SAGR, POLICY, null).status()).isEqualTo(Status.NO_TPA);
    }

    @Test
    void singleLinkedTpa_isUsed() {
        linkTpas(totalcare);

        TpaResolutionVM result = service.resolve(AL_SAGR, POLICY, null);

        assertThat(result.status()).isEqualTo(Status.RESOLVED);
        assertThat(result.tpaNphiesId()).isEqualTo("7001599658");
    }

    @Test
    void singleLinkedTpaWithoutNphiesId_isReportedInsteadOfGoingToThePayer() {
        linkTpas(tpa(3L, "Mednet", null));

        TpaResolutionVM result = service.resolve(AL_SAGR, POLICY, null);

        assertThat(result.status()).isEqualTo(Status.NPHIES_ID_MISSING);
        assertThat(result.tpaName()).isEqualTo("Mednet");
    }

    @Test
    void severalTpas_policyContractDecides() {
        linkTpas(totalcare, nextcare);
        contractsFor(contract(GuarantorType.TPA, nextcare.getId()));

        TpaResolutionVM result = service.resolve(AL_SAGR, POLICY, null);

        assertThat(result.status()).isEqualTo(Status.RESOLVED);
        assertThat(result.tpaNphiesId()).isEqualTo("7001580856");
    }

    @Test
    void severalTpas_withoutContract_needSelectionAndListCandidates() {
        linkTpas(totalcare, nextcare);
        contractsFor();

        TpaResolutionVM result = service.resolve(AL_SAGR, POLICY, null);

        assertThat(result.status()).isEqualTo(Status.SELECTION_REQUIRED);
        assertThat(result.tpaNphiesId()).isNull();
        assertThat(result.candidates()).extracting(TpaResolutionVM.Candidate::name)
                .containsExactly("Nextcare", "Totalcare");
    }

    @Test
    void severalTpas_contractsOnBothTpas_needSelection() {
        linkTpas(totalcare, nextcare);
        contractsFor(
                contract(GuarantorType.TPA, totalcare.getId()),
                contract(GuarantorType.TPA, nextcare.getId())
        );

        assertThat(service.resolve(AL_SAGR, POLICY, null).status()).isEqualTo(Status.SELECTION_REQUIRED);
    }

    @Test
    void severalTpas_insuranceContractDoesNotIdentifyATpa() {
        linkTpas(totalcare, nextcare);
        contractsFor(contract(GuarantorType.INSURANCE, totalcare.getId()));

        assertThat(service.resolve(AL_SAGR, POLICY, null).status()).isEqualTo(Status.SELECTION_REQUIRED);
    }

    @Test
    void preferredTpa_winsOverSetup() {
        linkTpas(totalcare, nextcare);

        TpaResolutionVM result = service.resolve(AL_SAGR, POLICY, " 7001599658 ");

        assertThat(result.status()).isEqualTo(Status.RESOLVED);
        assertThat(result.tpaNphiesId()).isEqualTo("7001599658");
        assertThat(result.tpaName()).isEqualTo("Totalcare");
    }

    @Test
    void noTpaMarkerFromCchi_isNotTreatedAsATpa() {
        linkTpas();

        assertThat(service.resolve(AL_SAGR, POLICY, "-1").status()).isEqualTo(Status.NO_TPA);
    }

    private void linkTpas(TpaDefinition... tpas) {
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(10L)).thenReturn(List.of(tpas));
    }

    private void contractsFor(CoverageContract... contracts) {
        List<CoverageClass> classes = java.util.Arrays.stream(contracts)
                .map(contract -> {
                    CoverageClass coverageClass = mock(CoverageClass.class);
                    when(coverageClass.getCoverageContract()).thenReturn(contract);
                    return coverageClass;
                })
                .toList();
        when(coverageClassRepository
                .findByIsActiveTrueAndCoverageContract_IsActiveTrueAndCoverageContract_InsurancePayerIdAndInsuranceCompanyPolicy_PolicyNumberIgnoreCase(
                        10L,
                        POLICY
                ))
                .thenReturn(classes);
    }

    private static CoverageContract contract(GuarantorType guarantorType, Long companyId) {
        CoverageContract contract = mock(CoverageContract.class);
        when(contract.getGuarantorType()).thenReturn(guarantorType);
        when(contract.getCompanyId()).thenReturn(companyId);
        return contract;
    }

    private static Payor payor(String tpaNphiesId) {
        Payor payor = mock(Payor.class);
        when(payor.getTpaNphiesId()).thenReturn(tpaNphiesId);
        return payor;
    }

    private static TpaDefinition tpa(Long id, String name, String nphiesId) {
        return TpaDefinition.builder().id(id).name(name).nphiesId(nphiesId).isActive(true).build();
    }
}
