package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Country;
import com.dazzle.asklepios.domain.CountryDistrict;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.enumeration.CountryName;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.dazzle.asklepios.repository.CountryDistrictRepository;
import com.dazzle.asklepios.repository.CountryRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.NphiesPayerSaveVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.NphiesPayerUpdateVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

        NphiesPayer updated = nphiesPayerService.update(vm);

        assertThat(updated.getNameEn()).isEqualTo("Tawuniya");
        assertThat(updated.getShortName()).isEqualTo("TAW");
        assertThat(updated.getEmail()).isEqualTo("info@example.com");
        verify(nphiesPayerRepository).save(existing);
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
                true
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
                true
        );
    }
}
