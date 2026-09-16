package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import com.dazzle.asklepios.repository.CountryDistrictRepository;
import com.dazzle.asklepios.repository.CountryRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.TpaDefinitionRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.tpadefinition.TpaDefinitionSaveVM;
import com.dazzle.asklepios.web.rest.vm.tpadefinition.TpaDefinitionUpdateVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TpaDefinitionServiceTest {

    @Mock
    private TpaDefinitionRepository tpaDefinitionRepository;

    @Mock
    private NphiesPayerRepository nphiesPayerRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private CountryDistrictRepository countryDistrictRepository;

    @InjectMocks
    private TpaDefinitionService tpaDefinitionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_success() {
        TpaDefinitionSaveVM vm = saveVm(List.of());

        when(tpaDefinitionRepository.existsByTpaCodeIgnoreCase("TPA-001")).thenReturn(false);
        when(tpaDefinitionRepository.save(any(TpaDefinition.class))).thenAnswer(invocation -> {
            TpaDefinition saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        TpaDefinition created = tpaDefinitionService.create(vm);

        assertThat(created.getId()).isEqualTo(10L);
        assertThat(created.getTpaCode()).isEqualTo("TPA-001");
        assertThat(created.getName()).isEqualTo("MedGulf TPA");
        assertThat(created.getGuarantorType()).isEqualTo(GuarantorType.TPA);
        assertThat(created.getIsActive()).isTrue();
        verify(tpaDefinitionRepository).save(any(TpaDefinition.class));
    }

    @Test
    void create_duplicateCode_throws() {
        when(tpaDefinitionRepository.existsByTpaCodeIgnoreCase("TPA-001")).thenReturn(true);

        assertThrows(BadRequestAlertException.class, () -> tpaDefinitionService.create(saveVm(List.of())));
        verify(tpaDefinitionRepository, never()).save(any());
    }

    @Test
    void create_inactiveTpaCannotLinkInsurance_throws() {
        TpaDefinitionSaveVM vm = new TpaDefinitionSaveVM(
                "TPA-001",
                "MedGulf TPA",
                GuarantorType.TPA,
                LocalDate.of(2026, 1, 1),
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(5L)
        );

        when(tpaDefinitionRepository.existsByTpaCodeIgnoreCase("TPA-001")).thenReturn(false);
        when(nphiesPayerRepository.findAllById(List.of(5L))).thenReturn(List.of(
                NphiesPayer.builder().id(5L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build()
        ));

        assertThrows(BadRequestAlertException.class, () -> tpaDefinitionService.create(vm));
        verify(tpaDefinitionRepository, never()).save(any());
    }

    @Test
    void toggle_activeLinkedInsurance_throws() {
        TpaDefinition tpa = TpaDefinition.builder()
                .id(3L)
                .tpaCode("TPA-001")
                .name("MedGulf TPA")
                .guarantorType(GuarantorType.TPA)
                .activationDate(LocalDate.of(2026, 1, 1))
                .isActive(true)
                .insuranceCompanies(new HashSet<>(List.of(
                        NphiesPayer.builder().id(5L).nphiesId("INS-1").nameEn("Tawuniya").isActive(true).build()
                )))
                .build();

        when(tpaDefinitionRepository.findById(3L)).thenReturn(Optional.of(tpa));

        assertThrows(BadRequestAlertException.class, () -> tpaDefinitionService.toggleIsActive(3L));
        verify(tpaDefinitionRepository, never()).save(any());
    }

    @Test
    void update_success() {
        TpaDefinition existing = TpaDefinition.builder()
                .id(3L)
                .tpaCode("TPA-001")
                .name("Old Name")
                .guarantorType(GuarantorType.TPA)
                .activationDate(LocalDate.of(2025, 1, 1))
                .isActive(true)
                .insuranceCompanies(new HashSet<>())
                .build();

        TpaDefinitionUpdateVM vm = new TpaDefinitionUpdateVM(
                3L,
                "TPA-001",
                "MedGulf TPA",
                GuarantorType.TPA,
                LocalDate.of(2026, 1, 1),
                true,
                "3001234567",
                null,
                null,
                "Riyadh",
                "0111111111",
                "tpa@example.com",
                null,
                List.of()
        );

        when(tpaDefinitionRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(tpaDefinitionRepository.save(any(TpaDefinition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TpaDefinition updated = tpaDefinitionService.update(vm);

        assertThat(updated.getName()).isEqualTo("MedGulf TPA");
        assertThat(updated.getTaxRegistrationNo()).isEqualTo("3001234567");
        verify(tpaDefinitionRepository).save(existing);
    }

    private TpaDefinitionSaveVM saveVm(List<Long> insuranceCompanyIds) {
        return new TpaDefinitionSaveVM(
                "TPA-001",
                "MedGulf TPA",
                GuarantorType.TPA,
                LocalDate.of(2026, 1, 1),
                true,
                null,
                null,
                null,
                null,
                null,
                "tpa@example.com",
                null,
                insuranceCompanyIds
        );
    }
}
