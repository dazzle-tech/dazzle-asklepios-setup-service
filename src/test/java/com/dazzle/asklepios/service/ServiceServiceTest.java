package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Service;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.ServiceCreateVM;
import com.dazzle.asklepios.web.rest.vm.ServiceResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceUpdateVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceService serviceService;

    private Service service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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
                .build();
    }

    @Test
    void testCreateService_Success() {
        var vm = new ServiceCreateVM(
                "MRI Scan", "MRI", "MRI-01", ServiceCategory.CONSULTATION,
                new BigDecimal("199.99"), Currency.USD, true, "tester"
        );

        when(serviceRepository.save(any(Service.class))).thenReturn(service);

        Service created = serviceService.create(vm);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("MRI Scan");
        assertThat(created.getCode()).isEqualTo("MRI-01");
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void testUpdateService_Success() {
        var vm = new ServiceUpdateVM(
                1L, "Updated Name", "UPD", "UPD-01",
                ServiceCategory.CONSUMABLE, new BigDecimal("500.00"),
                Currency.EUR, false, "modifier"
        );

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Service> updatedOpt = serviceService.update(1L, vm);

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
    }

    @Test
    void testUpdateService_NotFound_Throws() {
        var vm = new ServiceUpdateVM(
                99L, "x", null, "X", null, null, Currency.USD, true, "m"
        );

        when(serviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BadRequestAlertException.class, () -> serviceService.update(99L, vm));
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void testToggleIsActive() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<Service> toggled = serviceService.toggleIsActive(1L);

        assertThat(toggled).isPresent();
        assertThat(toggled.get().getIsActive()).isFalse();
    }

    @Test
    void testFindAll_Paginated_MapsToVM() {
        var pageable = PageRequest.of(0, 10);
        Page<Service> page = new PageImpl<>(List.of(service), pageable, 1);

        when(serviceRepository.findAll(pageable)).thenReturn(page);

        Page<ServiceResponseVM> result = serviceService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("MRI Scan");
        assertThat(result.getContent().get(0).code()).isEqualTo("MRI-01");
        verify(serviceRepository).findAll(pageable);
    }

    @Test
    void testFindByCategory_Paginated() {
        var pageable = PageRequest.of(0, 5);
        Page<Service> page = new PageImpl<>(List.of(service), pageable, 1);

        when(serviceRepository.findByCategory(ServiceCategory.CONSULTATION, pageable)).thenReturn(page);

        Page<ServiceResponseVM> result = serviceService.findByCategory(ServiceCategory.CONSULTATION, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).category()).isEqualTo(ServiceCategory.CONSULTATION);
        verify(serviceRepository).findByCategory(ServiceCategory.CONSULTATION, pageable);
    }

    @Test
    void testFindByCode_Paginated() {
        var pageable = PageRequest.of(0, 5);
        Page<Service> page = new PageImpl<>(List.of(service), pageable, 1);

        when(serviceRepository.findByCodeContainingIgnoreCase("MRI-01", pageable))
                .thenReturn(page);

        Page<ServiceResponseVM> result =
                serviceService.findByCodeContainingIgnoreCase("MRI-01", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("MRI-01");

        verify(serviceRepository).findByCodeContainingIgnoreCase("MRI-01", pageable);
    }


    @Test
    void testFindByNameContainingIgnoreCase_Paginated() {
        var pageable = PageRequest.of(0, 5);
        Page<Service> page = new PageImpl<>(List.of(service), pageable, 1);

        when(serviceRepository.findByNameContainingIgnoreCase("mri", pageable)).thenReturn(page);

        Page<ServiceResponseVM> result = serviceService.findByNameContainingIgnoreCase("mri", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("MRI Scan");
        verify(serviceRepository).findByNameContainingIgnoreCase("mri", pageable);
    }

    @Test
    void testExistsByNameIgnoreCase() {
        when(serviceRepository.existsByNameIgnoreCase("MRI Scan")).thenReturn(true);

        boolean exists = serviceService.existsByNameIgnoreCase("MRI Scan");

        assertThat(exists).isTrue();
        verify(serviceRepository).existsByNameIgnoreCase("MRI Scan");
    }

    @Test
    void testFindOne_Found() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));

        Optional<Service> result = serviceService.findOne(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(service);
        verify(serviceRepository).findById(1L);
    }

    @Test
    void testFindOne_NotFound() {
        when(serviceRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Service> result = serviceService.findOne(99L);

        assertThat(result).isEmpty();
        verify(serviceRepository).findById(99L);
    }
}
