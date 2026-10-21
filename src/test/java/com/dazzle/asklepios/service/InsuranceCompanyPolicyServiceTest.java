package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.InsuranceCompanyPolicy;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.repository.InsuranceCompanyPolicyRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.insurancecompanypolicy.InsuranceCompanyPolicySaveVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InsuranceCompanyPolicyServiceTest {

    @Mock
    private InsuranceCompanyPolicyRepository insuranceCompanyPolicyRepository;

    @Mock
    private NphiesPayerRepository nphiesPayerRepository;

    @InjectMocks
    private InsuranceCompanyPolicyService insuranceCompanyPolicyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_success() {
        NphiesPayer company = company(8L);
        when(nphiesPayerRepository.findById(8L)).thenReturn(Optional.of(company));
        when(insuranceCompanyPolicyRepository.existsByInsuranceCompany_IdAndPolicyCodeIgnoreCase(8L, "POL-01")).thenReturn(false);
        when(insuranceCompanyPolicyRepository.existsByInsuranceCompany_IdAndPolicyNumberIgnoreCase(8L, "PN-100")).thenReturn(false);
        when(insuranceCompanyPolicyRepository.save(any(InsuranceCompanyPolicy.class))).thenAnswer(invocation -> {
            InsuranceCompanyPolicy saved = invocation.getArgument(0);
            saved.setId(21L);
            return saved;
        });

        InsuranceCompanyPolicy created = insuranceCompanyPolicyService.create(8L, saveVm());

        assertThat(created.getId()).isEqualTo(21L);
        assertThat(created.getInsuranceCompany().getId()).isEqualTo(8L);
        assertThat(created.getPolicyName()).isEqualTo("Gold Plan");
        assertThat(created.getPolicyNumber()).isEqualTo("PN-100");
        assertThat(created.getPolicyCode()).isEqualTo("POL-01");
        assertThat(created.getValidFrom()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(created.getValidTo()).isEqualTo(LocalDate.of(2026, 12, 31));
        verify(insuranceCompanyPolicyRepository).save(any(InsuranceCompanyPolicy.class));
    }

    @Test
    void create_missingCompany_throws() {
        when(nphiesPayerRepository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(NotFoundAlertException.class, () -> insuranceCompanyPolicyService.create(8L, saveVm()));
        verify(insuranceCompanyPolicyRepository, never()).save(any());
    }

    @Test
    void create_duplicateCode_throws() {
        when(nphiesPayerRepository.findById(8L)).thenReturn(Optional.of(company(8L)));
        when(insuranceCompanyPolicyRepository.existsByInsuranceCompany_IdAndPolicyCodeIgnoreCase(8L, "POL-01")).thenReturn(true);

        assertThrows(BadRequestAlertException.class, () -> insuranceCompanyPolicyService.create(8L, saveVm()));
        verify(insuranceCompanyPolicyRepository, never()).save(any());
    }

    @Test
    void create_invalidRange_throws() {
        when(nphiesPayerRepository.findById(8L)).thenReturn(Optional.of(company(8L)));
        InsuranceCompanyPolicySaveVM vm = new InsuranceCompanyPolicySaveVM(
                "Gold Plan",
                "PN-100",
                "POL-01",
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 1)
        );

        assertThrows(BadRequestAlertException.class, () -> insuranceCompanyPolicyService.create(8L, vm));
        verify(insuranceCompanyPolicyRepository, never()).save(any());
    }

    @Test
    void findByInsuranceCompanyId_returnsPolicies() {
        InsuranceCompanyPolicy policy = InsuranceCompanyPolicy.builder()
                .id(21L)
                .insuranceCompany(company(8L))
                .policyName("Gold Plan")
                .policyNumber("PN-100")
                .policyCode("POL-01")
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(LocalDate.of(2026, 12, 31))
                .build();
        when(nphiesPayerRepository.findById(8L)).thenReturn(Optional.of(company(8L)));
        when(insuranceCompanyPolicyRepository.findByInsuranceCompany_IdOrderByValidFromDescIdDesc(8L))
                .thenReturn(List.of(policy));

        List<InsuranceCompanyPolicy> policies = insuranceCompanyPolicyService.findByInsuranceCompanyId(8L);

        assertThat(policies).extracting(InsuranceCompanyPolicy::getPolicyCode).containsExactly("POL-01");
    }

    private InsuranceCompanyPolicySaveVM saveVm() {
        return new InsuranceCompanyPolicySaveVM(
                " Gold Plan ",
                " PN-100 ",
                " POL-01 ",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    private NphiesPayer company(Long id) {
        return NphiesPayer.builder()
                .id(id)
                .nphiesId("NPH-" + id)
                .nameEn("Bupa")
                .isActive(true)
                .build();
    }
}
