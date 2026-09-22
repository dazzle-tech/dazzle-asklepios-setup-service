package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Country;
import com.dazzle.asklepios.domain.CountryDistrict;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.domain.enumeration.CountryName;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.dazzle.asklepios.repository.CountryDistrictRepository;
import com.dazzle.asklepios.repository.CountryRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.TpaDefinitionRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.NphiesPayerSaveVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.NphiesPayerUpdateVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NphiesPayerServiceTest {

    @Mock
    private NphiesPayerRepository nphiesPayerRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private CountryDistrictRepository countryDistrictRepository;

    @Mock
    private TpaDefinitionRepository tpaDefinitionRepository;

    @InjectMocks
    private NphiesPayerService nphiesPayerService;

    private Facility facility;
    private Country country;
    private CountryDistrict city;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        facility = Facility.builder()
                .id(1L)
                .name("Main Hospital")
                .code("FAC-1")
                .type(FacilityType.HOSPITAL)
                .isActive(true)
                .build();

        country = Country.builder()
                .id(10L)
                .name(CountryName.SAUDI_ARABIA)
                .code("SA")
                .isActive(true)
                .build();

        city = CountryDistrict.builder()
                .id(20L)
                .name("Riyadh")
                .code("RYD")
                .country(country)
                .isActive(true)
                .build();
    }

    @Test
    void create_success() {
        NphiesPayerSaveVM vm = saveVm("INS-001", 1L, 10L, 20L, "https://example.com");

        when(nphiesPayerRepository.existsByNphiesIdIgnoreCase("INS-001")).thenReturn(false);
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));
        when(countryRepository.findById(10L)).thenReturn(Optional.of(country));
        when(countryDistrictRepository.findById(20L)).thenReturn(Optional.of(city));
        when(nphiesPayerRepository.save(any(NphiesPayer.class))).thenAnswer(invocation -> {
            NphiesPayer saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });
        when(nphiesPayerRepository.findById(100L)).thenAnswer(invocation -> Optional.of(
                NphiesPayer.builder()
                        .id(100L)
                        .nphiesId("INS-001")
                        .nameEn("Tawuniya")
                        .facility(facility)
                        .country(country)
                        .city(city)
                        .isActive(true)
                        .build()
        ));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(100L)).thenReturn(List.of());

        NphiesPayer created = nphiesPayerService.create(vm);

        assertThat(created.getId()).isEqualTo(100L);
        assertThat(created.getNphiesId()).isEqualTo("INS-001");
        assertThat(created.getNameEn()).isEqualTo("Tawuniya");
        assertThat(created.getFacility()).isEqualTo(facility);
        assertThat(created.getCountry()).isEqualTo(country);
        assertThat(created.getCity()).isEqualTo(city);
        assertThat(created.getIsActive()).isTrue();
        verify(nphiesPayerRepository).save(any(NphiesPayer.class));
    }

    @Test
    void findAll_attachesLinkedTpas() {
        NphiesPayer payer = NphiesPayer.builder()
                .id(1L)
                .nphiesId("INS-001")
                .nameEn("Tawuniya")
                .isActive(true)
                .build();
        TpaDefinition tpa = TpaDefinition.builder()
                .id(9L)
                .tpaCode("TPA-1")
                .name("Medgulf TPA")
                .isActive(true)
                .build();
        tpa.setInsuranceCompanies(new HashSet<>(Set.of(payer)));

        when(nphiesPayerRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(payer)));
        when(tpaDefinitionRepository.findByInsuranceCompanies_IdIn(anyCollection())).thenReturn(List.of(tpa));

        Page<NphiesPayer> page = nphiesPayerService.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTpas()).extracting(TpaDefinition::getId).containsExactly(9L);
    }

    @Test
    void create_duplicateNphiesId_throws() {
        NphiesPayerSaveVM vm = saveVm("INS-001", 1L, null, null, null);
        when(nphiesPayerRepository.existsByNphiesIdIgnoreCase("INS-001")).thenReturn(true);

        assertThrows(BadRequestAlertException.class, () -> nphiesPayerService.create(vm));
        verify(nphiesPayerRepository, never()).save(any());
    }

    @Test
    void create_inactiveFacility_throws() {
        facility.setIsActive(false);
        NphiesPayerSaveVM vm = saveVm("INS-001", 1L, null, null, null);

        when(nphiesPayerRepository.existsByNphiesIdIgnoreCase("INS-001")).thenReturn(false);
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));

        assertThrows(BadRequestAlertException.class, () -> nphiesPayerService.create(vm));
        verify(nphiesPayerRepository, never()).save(any());
    }

    @Test
    void create_invalidWebsite_throws() {
        NphiesPayerSaveVM vm = saveVm("INS-001", 1L, null, null, "example.com");

        when(nphiesPayerRepository.existsByNphiesIdIgnoreCase("INS-001")).thenReturn(false);

        assertThrows(BadRequestAlertException.class, () -> nphiesPayerService.create(vm));
        verify(nphiesPayerRepository, never()).save(any());
    }

    @Test
    void create_cityWithoutCountry_throws() {
        NphiesPayerSaveVM vm = saveVm("INS-001", 1L, null, 20L, null);

        when(nphiesPayerRepository.existsByNphiesIdIgnoreCase("INS-001")).thenReturn(false);

        assertThrows(BadRequestAlertException.class, () -> nphiesPayerService.create(vm));
        verify(nphiesPayerRepository, never()).save(any());
    }

    @Test
    void update_success() {
        NphiesPayer existing = NphiesPayer.builder()
                .id(5L)
                .nphiesId("INS-001")
                .nameEn("Old Name")
                .facility(facility)
                .isActive(true)
                .build();

        NphiesPayerUpdateVM vm = updateVm(5L, "INS-001", 1L, 10L, 20L);

        when(nphiesPayerRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));
        when(countryRepository.findById(10L)).thenReturn(Optional.of(country));
        when(countryDistrictRepository.findById(20L)).thenReturn(Optional.of(city));
        when(nphiesPayerRepository.save(any(NphiesPayer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(nphiesPayerRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(5L)).thenReturn(List.of());

        NphiesPayer updated = nphiesPayerService.update(vm);

        assertThat(updated.getNameEn()).isEqualTo("Tawuniya");
        assertThat(updated.getShortName()).isEqualTo("TAW");
        assertThat(updated.getEmail()).isEqualTo("info@example.com");
        verify(nphiesPayerRepository).save(existing);
    }

    @Test
    void findAvailableTpas_returnsUnlinkedActive() {
        TpaDefinition available = TpaDefinition.builder()
                .id(2L)
                .tpaCode("TPA-2")
                .name("Available TPA")
                .isActive(true)
                .build();

        when(nphiesPayerRepository.existsById(5L)).thenReturn(true);
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(5L)).thenReturn(List.of());
        when(tpaDefinitionRepository.findByIsActiveTrue()).thenReturn(List.of(available));

        List<TpaDefinition> result = nphiesPayerService.findAvailableTpas(5L);

        assertThat(result).extracting(TpaDefinition::getId).containsExactly(2L);
        assertThat(result).extracting(TpaDefinition::getName).containsExactly("Available TPA");
    }

    @Test
    void findAvailableTpas_payerNotFound_throws() {
        when(nphiesPayerRepository.existsById(99L)).thenReturn(false);

        assertThrows(BadRequestAlertException.class, () -> nphiesPayerService.findAvailableTpas(99L));
        verify(tpaDefinitionRepository, never()).findByIsActiveTrue();
        verify(tpaDefinitionRepository, never()).findByInsuranceCompanies_Id(99L);
    }

    @Test
    void updateTpas_ignoresDuplicateIds() {
        NphiesPayer payer = NphiesPayer.builder()
                .id(5L)
                .nphiesId("INS-001")
                .nameEn("Tawuniya")
                .isActive(true)
                .build();
        TpaDefinition tpa = TpaDefinition.builder()
                .id(9L)
                .tpaCode("TPA-1")
                .name("Medgulf TPA")
                .isActive(true)
                .insuranceCompanies(new HashSet<>())
                .build();

        when(nphiesPayerRepository.findById(5L)).thenReturn(Optional.of(payer));
        when(tpaDefinitionRepository.findByInsuranceCompanies_Id(5L)).thenReturn(List.of());
        when(tpaDefinitionRepository.findByIdIn(anyCollection())).thenReturn(List.of(tpa));
        when(tpaDefinitionRepository.saveAll(anyCollection())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tpaDefinitionRepository.findByInsuranceCompanies_IdIn(anyCollection())).thenReturn(List.of(tpa));
        when(nphiesPayerRepository.findDistinctByIdIn(anyCollection())).thenReturn(List.of(payer));

        NphiesPayer updated = nphiesPayerService.updateTpas(5L, List.of(9L, 9L));

        assertThat(tpa.getInsuranceCompanies()).extracting(NphiesPayer::getId).containsExactly(5L);
        assertThat(updated.getTpas()).extracting(TpaDefinition::getId).containsExactly(9L);
    }

    @Test
    void findAvailableChildCompanies_returnsUnlinkedActive() {
        NphiesPayer available = NphiesPayer.builder()
                .id(8L)
                .nphiesId("INS-008")
                .nameEn("Bupa")
                .isActive(true)
                .build();

        when(nphiesPayerRepository.existsById(5L)).thenReturn(true);
        when(nphiesPayerRepository.findDistinctByParentCompanies_IdNotNull()).thenReturn(List.of());
        when(nphiesPayerRepository.findByChildCompanies_Id(5L)).thenReturn(List.of());
        when(nphiesPayerRepository.findByIsActiveTrue()).thenReturn(List.of(available));

        List<NphiesPayer> result = nphiesPayerService.findAvailableChildCompanies(5L);

        assertThat(result).extracting(NphiesPayer::getId).containsExactly(8L);
    }

    @Test
    void findAvailableChildCompanies_excludesSelfAndAlreadyLinkedChildren() {
        NphiesPayer parent = NphiesPayer.builder()
                .id(5L)
                .nphiesId("INS-005")
                .nameEn("Tawuniya")
                .isActive(true)
                .build();
        NphiesPayer alreadyChild = NphiesPayer.builder()
                .id(8L)
                .nphiesId("INS-008")
                .nameEn("Bupa")
                .isActive(true)
                .build();
        NphiesPayer available = NphiesPayer.builder()
                .id(9L)
                .nphiesId("INS-009")
                .nameEn("Medgulf")
                .isActive(true)
                .build();

        when(nphiesPayerRepository.existsById(5L)).thenReturn(true);
        when(nphiesPayerRepository.findDistinctByParentCompanies_IdNotNull()).thenReturn(List.of(alreadyChild));
        when(nphiesPayerRepository.findByChildCompanies_Id(5L)).thenReturn(List.of());
        when(nphiesPayerRepository.findByIsActiveTrue()).thenReturn(List.of(parent, alreadyChild, available));

        List<NphiesPayer> result = nphiesPayerService.findAvailableChildCompanies(5L);

        assertThat(result).extracting(NphiesPayer::getId).containsExactly(9L);
    }

    @Test
    void updateChildCompanies_linksSelectedCompanies() {
        NphiesPayer payer = NphiesPayer.builder()
                .id(5L)
                .nphiesId("INS-001")
                .nameEn("Tawuniya")
                .isActive(true)
                .childCompanies(new HashSet<>())
                .build();
        NphiesPayer child = NphiesPayer.builder()
                .id(8L)
                .nphiesId("INS-008")
                .nameEn("Bupa")
                .isActive(true)
                .build();
        payer.setChildCompanies(new HashSet<>(Set.of(child)));

        when(nphiesPayerRepository.findById(5L)).thenReturn(Optional.of(payer));
        when(nphiesPayerRepository.findByParentCompanies_Id(5L)).thenReturn(List.of());
        when(nphiesPayerRepository.findByIdIn(anyCollection())).thenReturn(List.of(child));
        when(nphiesPayerRepository.findByChildCompanies_Id(8L)).thenReturn(List.of());
        when(nphiesPayerRepository.findByChildCompanies_Id(5L)).thenReturn(List.of());
        when(nphiesPayerRepository.save(any(NphiesPayer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tpaDefinitionRepository.findByInsuranceCompanies_IdIn(anyCollection())).thenReturn(List.of());
        when(nphiesPayerRepository.findDistinctByIdIn(anyCollection())).thenReturn(List.of(payer));

        NphiesPayer updated = nphiesPayerService.updateChildCompanies(5L, List.of(8L, 8L));

        assertThat(updated.getChildCompanies()).extracting(NphiesPayer::getId).containsExactly(8L);
        verify(nphiesPayerRepository).save(payer);
    }

    @Test
    void updateChildCompanies_rejectsSelfLink() {
        NphiesPayer payer = NphiesPayer.builder()
                .id(5L)
                .nphiesId("INS-001")
                .nameEn("Tawuniya")
                .isActive(true)
                .childCompanies(new HashSet<>())
                .build();

        when(nphiesPayerRepository.findById(5L)).thenReturn(Optional.of(payer));

        assertThrows(BadRequestAlertException.class, () -> nphiesPayerService.updateChildCompanies(5L, List.of(5L)));
        verify(nphiesPayerRepository, never()).save(any());
    }

    @Test
    void updateChildCompanies_rejectsCompanyAlreadyUnderAnotherParent() {
        NphiesPayer payer = NphiesPayer.builder()
                .id(5L)
                .nphiesId("INS-001")
                .nameEn("Tawuniya")
                .isActive(true)
                .childCompanies(new HashSet<>())
                .build();
        NphiesPayer otherParent = NphiesPayer.builder()
                .id(4L)
                .nphiesId("INS-004")
                .nameEn("Other Parent")
                .isActive(true)
                .build();
        NphiesPayer child = NphiesPayer.builder()
                .id(8L)
                .nphiesId("INS-008")
                .nameEn("Bupa")
                .isActive(true)
                .build();

        when(nphiesPayerRepository.findById(5L)).thenReturn(Optional.of(payer));
        when(nphiesPayerRepository.findByParentCompanies_Id(5L)).thenReturn(List.of());
        when(nphiesPayerRepository.findByIdIn(anyCollection())).thenReturn(List.of(child));
        when(nphiesPayerRepository.findByChildCompanies_Id(8L)).thenReturn(List.of(otherParent));

        assertThrows(BadRequestAlertException.class, () -> nphiesPayerService.updateChildCompanies(5L, List.of(8L)));
        verify(nphiesPayerRepository, never()).save(any());
    }

    @Test
    void update_notFound_throws() {
        NphiesPayerUpdateVM vm = updateVm(99L, "INS-001", 1L, null, null);
        when(nphiesPayerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BadRequestAlertException.class, () -> nphiesPayerService.update(vm));
    }

    private NphiesPayerSaveVM saveVm(
            String nphiesId,
            Long facilityId,
            Long countryId,
            Long cityId,
            String website
    ) {
        return new NphiesPayerSaveVM(
                nphiesId,
                "Tawuniya",
                "التعاونية",
                "TAW",
                facilityId,
                "LIC-1",
                "CR-1",
                "VAT-1",
                "7000000001",
                "Riyadh HQ",
                countryId,
                cityId,
                "11564",
                "Ahmed",
                "0111234567",
                "0501234567",
                "info@example.com",
                website,
                true,
                null,
                List.of(),
                List.of()
        );
    }

    private NphiesPayerUpdateVM updateVm(
            Long id,
            String nphiesId,
            Long facilityId,
            Long countryId,
            Long cityId
    ) {
        return new NphiesPayerUpdateVM(
                id,
                nphiesId,
                "Tawuniya",
                "التعاونية",
                "TAW",
                facilityId,
                "LIC-1",
                "CR-1",
                "VAT-1",
                "7000000001",
                "Riyadh HQ",
                countryId,
                cityId,
                "11564",
                "Ahmed",
                "0111234567",
                "0501234567",
                "info@example.com",
                "https://example.com",
                true,
                null,
                List.of(),
                List.of()
        );
    }
}
