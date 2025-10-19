package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Service;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.ServiceCreateVM;
import com.dazzle.asklepios.web.rest.vm.ServiceResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceUpdateVM;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private EntityManager em;

    @InjectMocks
    private ServiceService serviceService;

    private Service service;
    private Facility facility;
    private final Long facilityId = 42L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        facility = new Facility();
        facility.setId(facilityId);
        facility.setName("Test Facility");

        service = Service.builder()
                .id(1L)
                .name("MRI Scan")
                .abbreviation("MRI")
                .code("MRI-01")
                .category(ServiceCategory.CONSULTATION)
                .price(new BigDecimal("199.99"))
                .currency(Currency.USD)
                .isActive(true)
                .createdBy("tester")
                .lastModifiedBy("tester")
                .lastModifiedDate(Instant.now())
                .facility(facility)
                .build();

        // mock getReference(Facility.class, facilityId)
        when(em.getReference(eq(Facility.class), eq(facilityId))).thenReturn(facility);
    }

    @Test
    void testCreateService_Success() {
        var vm = new ServiceCreateVM(
                "MRI Scan", "MRI", "MRI-01", ServiceCategory.CONSULTATION,
                new BigDecimal("199.99"), Currency.USD, true, "tester",
                facilityId // حتى لو الخدمة لا تستخدمه، لا يضر وجوده
        );

        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> {
            Service s = inv.getArgument(0);
            // simulate DB assigned fields if needed
            s.setId(1L);
            return s;
        });

        Service created = serviceService.create(facilityId, vm);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("MRI Scan");
        assertThat(created.getCode()).isEqualTo("MRI-01");
        assertThat(created.getFacility()).isNotNull();
        assertThat(created.getFacility().getId()).isEqualTo(facilityId);

        verify(em).getReference(Facility.class, facilityId);
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void testUpdateService_Success() {
        var vm = new ServiceUpdateVM(
                1L, "Updated Name", "UPD", "UPD-01",
                ServiceCategory.CONSUMABLE, new BigDecimal("500.00"),
                Currency.EUR, false, "modifier",
                facilityId
        );

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Service> updatedOpt = serviceService.update(1L, facilityId, vm);

        assertThat(updatedOpt).isPresent();
        Service updated = updatedOpt.get();
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getAbbreviation()).isEqualTo("UPD");
        assertThat(updated.getCode()).isEqualTo("UPD-01");
        assertThat(updated.getCategory()).isEqualTo(ServiceCategory.CONSUMABLE);
        assertThat(updated.getPrice()).isEqualByComparingTo("500.00");
        assertThat(updated.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(updated.getIsActive()).isFalse();
        assertThat(updated.getLastModifiedBy()).isEqualTo("modifier");
        assertThat(updated.getFacility().getId()).isEqualTo(facilityId);

        verify(serviceRepository).findById(1L);
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void testUpdateService_NotFound_Throws() {
        var vm = new ServiceUpdateVM(
                99L, "x", null, "X", null, null, Currency.USD, true, "m", facilityId
        );

        when(serviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BadRequestAlertException.class, () -> serviceService.update(99L, facilityId, vm));
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void testToggleIsActive_ScopedToFacility() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<Service> toggled = serviceService.toggleIsActive(1L, facilityId);

        assertThat(toggled).isPresent();
        assertThat(toggled.get().getIsActive()).isFalse();
        verify(serviceRepository).findById(1L);
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void testFindAll_Paginated_MapsToVM_Scoped() {
        var pageable = PageRequest.of(0, 10);
        Page<Service> page = new PageImpl<>(List.of(service), pageable, 1);

        when(serviceRepository.findByFacility_Id(facilityId, pageable)).thenReturn(page);

        Page<ServiceResponseVM> result = serviceService.findAll(facilityId, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("MRI Scan");
        assertThat(result.getContent().get(0).code()).isEqualTo("MRI-01");
        verify(serviceRepository).findByFacility_Id(facilityId, pageable);
    }

    @Test
    void testFindByCategory_Paginated_Scoped() {
        var pageable = PageRequest.of(0, 5);
        Page<Service> page = new PageImpl<>(List.of(service), pageable, 1);

        when(serviceRepository.findByFacility_IdAndCategory(facilityId, ServiceCategory.CONSULTATION, pageable))
                .thenReturn(page);

        Page<ServiceResponseVM> result =
                serviceService.findByCategory(facilityId, ServiceCategory.CONSULTATION, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).category()).isEqualTo(ServiceCategory.CONSULTATION);
        verify(serviceRepository).findByFacility_IdAndCategory(facilityId, ServiceCategory.CONSULTATION, pageable);
    }

    @Test
    void testFindByCode_Paginated_Scoped() {
        var pageable = PageRequest.of(0, 5);
        Page<Service> page = new PageImpl<>(List.of(service), pageable, 1);

        when(serviceRepository.findByFacility_IdAndCodeContainingIgnoreCase(facilityId, "MRI-01", pageable))
                .thenReturn(page);

        Page<ServiceResponseVM> result =
                serviceService.findByCodeContainingIgnoreCase(facilityId, "MRI-01", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("MRI-01");
        verify(serviceRepository).findByFacility_IdAndCodeContainingIgnoreCase(facilityId, "MRI-01", pageable);
    }

    @Test
    void testFindByNameContainingIgnoreCase_Paginated_Scoped() {
        var pageable = PageRequest.of(0, 5);
        Page<Service> page = new PageImpl<>(List.of(service), pageable, 1);

        when(serviceRepository.findByFacility_IdAndNameContainingIgnoreCase(facilityId, "mri", pageable))
                .thenReturn(page);

        Page<ServiceResponseVM> result =
                serviceService.findByNameContainingIgnoreCase(facilityId, "mri", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("MRI Scan");
        verify(serviceRepository).findByFacility_IdAndNameContainingIgnoreCase(facilityId, "mri", pageable);
    }

    @Test
    void testExistsByNameIgnoreCase_Scoped() {
        when(serviceRepository.existsByFacility_IdAndNameIgnoreCase(facilityId, "MRI Scan")).thenReturn(true);

        boolean exists = serviceService.existsByNameIgnoreCase(facilityId, "MRI Scan");

        assertThat(exists).isTrue();
        verify(serviceRepository).existsByFacility_IdAndNameIgnoreCase(facilityId, "MRI Scan");
    }

    @Test
    void testFindOne_Found_Scoped() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));

        Optional<Service> result = serviceService.findOne(1L, facilityId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(serviceRepository).findById(1L);
    }

    @Test
    void testFindOne_NotFoundOrDifferentFacility_Scoped() {
        // خدمة منشأتها مختلفة
        Facility other = new Facility();
        other.setId(99L);
        other.setName("Other");

        Service svcOtherFacility = Service.builder()
                .id(999L)
                .name("Other")
                .code("O-1")
                .currency(Currency.USD)
                .isActive(true)
                .createdBy("t")
                .facility(other)
                .build();

        when(serviceRepository.findById(999L)).thenReturn(Optional.of(svcOtherFacility));

        Optional<Service> result = serviceService.findOne(999L, facilityId);

        assertThat(result).isEmpty();
        verify(serviceRepository).findById(999L);
    }
}
