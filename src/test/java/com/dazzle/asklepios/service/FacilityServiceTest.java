package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.web.rest.vm.FacilityCreateVM;
import com.dazzle.asklepios.web.rest.vm.FacilityUpdateVM;
import com.dazzle.asklepios.web.rest.vm.FacilityResponseVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doNothing;


class FacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @InjectMocks
    private FacilityService facilityService;

    private Facility facility;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        facility = Facility.builder()
                .id(1L)
                .name("Facility One")
                .code("FAC001")
                .type(FacilityType.HOSPITAL)
                .emailAddress("test@facility.com")
                .phone1("123456789")
                .phone2("987654321")
                .fax("111-222")
                .addressId("ADDR001")
                .defaultCurrency(Currency.USD)
                .build();
    }

    @Test
    void testCreateFacility_Success() {
        FacilityCreateVM vm = new FacilityCreateVM(
                "Facility One",
                "FAC001",
                FacilityType.HOSPITAL,
                "test@facility.com",
                "123456789",
                "987654321",
                "111-222",
                "ADDR001",
                Currency.USD,
                null,
                null
        );

        when(facilityRepository.save(any(Facility.class))).thenReturn(facility);

        FacilityResponseVM result = facilityService.create(vm);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Facility One");
        assertThat(result.type()).isEqualTo(FacilityType.HOSPITAL);
        assertThat(result.defaultCurrency()).isEqualTo(Currency.USD);

        verify(facilityRepository).save(any(Facility.class));
    }

    @Test
    void testCreateFacility_Failure_NullFacility() {
        FacilityCreateVM vm = new FacilityCreateVM(
                null, null, null, null, null, null, null, null, null, null, null
        );

        when(facilityRepository.save(any(Facility.class)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST));

        assertThrows(ResponseStatusException.class, () -> facilityService.create(vm));
        verify(facilityRepository).save(any(Facility.class));
    }

    @Test
    void testUpdateFacility_Success() {
        FacilityUpdateVM vm = new FacilityUpdateVM(
                1L, "Updated Facility", "FAC001", FacilityType.CLINIC,
                "updated@facility.com", "222333444", "555666777",
                "333-444", "ADDR002", Currency.EUR, true
        );

        when(facilityRepository.findById(facility.getId())).thenReturn(Optional.of(facility));
        when(facilityRepository.save(any(Facility.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Facility> updated = facilityService.update(facility.getId(), vm);

        assertThat(updated).isPresent();
        Facility u = updated.get();
        assertThat(u.getName()).isEqualTo("Updated Facility");
        assertThat(u.getType()).isEqualTo(FacilityType.CLINIC);
        assertThat(u.getEmailAddress()).isEqualTo("updated@facility.com");

        verify(facilityRepository).findById(facility.getId());
        verify(facilityRepository).save(any(Facility.class));
    }

    @Test
    void testUpdateFacility_NotFound() {
        FacilityUpdateVM vm = new FacilityUpdateVM(
                99L, "Name", "CODE99", FacilityType.CLINIC,
                null, null, null, null, null, null, false
        );

        when(facilityRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Facility> updated = facilityService.update(99L, vm);

        assertThat(updated).isEmpty();
        verify(facilityRepository).findById(99L);
        verify(facilityRepository, never()).save(any());
    }

    @Test
    void testFindAllFacilities() {
        when(facilityRepository.findAll()).thenReturn(List.of(facility));

        List<FacilityResponseVM> result = facilityService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Facility One");
        verify(facilityRepository).findAll();
    }

    @Test
    void testFindFacilityById_Found() {
        when(facilityRepository.findById(facility.getId())).thenReturn(Optional.of(facility));

        Optional<FacilityResponseVM> result = facilityService.findOne(facility.getId());

        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Facility One");
        verify(facilityRepository).findById(facility.getId());
    }

    @Test
    void testFindFacilityById_NotFound() {
        when(facilityRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<FacilityResponseVM> result = facilityService.findOne(99L);

        assertThat(result).isEmpty();
        verify(facilityRepository).findById(99L);
    }

    @Test
    void testDeleteFacility_Success() {
        when(facilityRepository.existsById(facility.getId())).thenReturn(true);
        doNothing().when(facilityRepository).deleteById(facility.getId());

        boolean deleted = facilityService.delete(facility.getId());

        assertThat(deleted).isTrue();
        verify(facilityRepository).deleteById(facility.getId());
    }

    @Test
    void testDeleteFacility_NotFound() {
        when(facilityRepository.existsById(99L)).thenReturn(false);

        boolean deleted = facilityService.delete(99L);

        assertThat(deleted).isFalse();
        verify(facilityRepository, never()).deleteById(99L);
    }
}
