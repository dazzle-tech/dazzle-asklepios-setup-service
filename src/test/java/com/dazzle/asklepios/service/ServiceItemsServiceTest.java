package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Service;
import com.dazzle.asklepios.domain.ServiceItems;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.domain.enumeration.ServiceItemsType;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.ServiceItemsRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.DepartmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceItemsCreateVM;
import com.dazzle.asklepios.web.rest.vm.ServiceItemsResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceItemsUpdateVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ServiceItemsServiceTest {

    @Mock private ServiceItemsRepository serviceItemsRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private DepartmentsRepository departmentsRepository;

    @InjectMocks
    private ServiceItemsService serviceItemsService;

    private Service service;
    private ServiceItems item;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        service = Service.builder()
                .id(10L)
                .name("MRI")
                .code("MRI-01")
                .category(ServiceCategory.CONSULTATION)
                .currency(Currency.USD)
                .isActive(true)
                .createdBy("tester")
                .build();

        item = ServiceItems.builder()
                .id(100L)
                .type(ServiceItemsType.DEPARTMENTS) // service supports DEPARTMENTS only in the finder
                .sourceId(55L)
                .service(service)
                .createdBy("tester")

                .lastModifiedBy("tester")
                .lastModifiedDate(Instant.now())
                .isActive(true)
                .build();
    }

    @Test
    void testCreate_Success() {
        var vm = new ServiceItemsCreateVM(
                ServiceItemsType.DEPARTMENTS,
                55L,
                10L, // serviceId required
                "tester",
                null,
                true);

        when(serviceRepository.findById(10L)).thenReturn(Optional.of(service));
        when(serviceItemsRepository.save(any(ServiceItems.class))).thenReturn(item);

        ServiceItems created = serviceItemsService.create(vm);

        assertThat(created).isNotNull();
        assertThat(created.getType()).isEqualTo(ServiceItemsType.DEPARTMENTS);
        assertThat(created.getSourceId()).isEqualTo(55L);
        assertThat(created.getService()).isEqualTo(service);
        verify(serviceRepository).findById(10L);
        verify(serviceItemsRepository).save(any(ServiceItems.class));
    }

    @Test
    void testCreate_ServiceNotFound_ThrowsResponseStatus() {
        var vm = new ServiceItemsCreateVM(
                ServiceItemsType.DEPARTMENTS, 55L, 999L,
                "tester", null, true
        );

        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> serviceItemsService.create(vm));
        verify(serviceItemsRepository, never()).save(any());
    }

    @Test
    void testUpdate_Success() {
        var vm = new ServiceItemsUpdateVM(
                100L,
                ServiceItemsType.DEPARTMENTS,
                77L,
                10L,
                false,
                "modifier",
                null
        );

        when(serviceItemsRepository.findById(100L)).thenReturn(Optional.of(item));
        when(serviceRepository.findById(10L)).thenReturn(Optional.of(service));
        when(serviceItemsRepository.save(any(ServiceItems.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<ServiceItems> updatedOpt = serviceItemsService.update(100L, vm);

        assertThat(updatedOpt).isPresent();
        ServiceItems updated = updatedOpt.get();
        assertThat(updated.getType()).isEqualTo(ServiceItemsType.DEPARTMENTS);
        assertThat(updated.getSourceId()).isEqualTo(77L);
        assertThat(updated.getService()).isEqualTo(service);
        assertThat(updated.getIsActive()).isFalse();
        assertThat(updated.getLastModifiedBy()).isEqualTo("modifier");
        verify(serviceItemsRepository).save(any(ServiceItems.class));
    }

    @Test
    void testUpdate_ServiceItemsNotFound_Throws() {
        var vm = new ServiceItemsUpdateVM(999L, ServiceItemsType.DEPARTMENTS, 1L, 10L, true, "m", null);

        when(serviceItemsRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BadRequestAlertException.class, () -> serviceItemsService.update(999L, vm));
        verify(serviceRepository, never()).findById(anyLong());
        verify(serviceItemsRepository, never()).save(any());
    }

    @Test
    void testUpdate_ServiceNotFound_Throws() {
        var vm = new ServiceItemsUpdateVM(100L, ServiceItemsType.DEPARTMENTS, 1L, 999L, true, "m", null);

        when(serviceItemsRepository.findById(100L)).thenReturn(Optional.of(item));
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BadRequestAlertException.class, () -> serviceItemsService.update(100L, vm));
        verify(serviceItemsRepository, never()).save(any());
    }

    @Test
    void testFindAll_NoPagination() {
        when(serviceItemsRepository.findAll()).thenReturn(List.of(item));

        var list = serviceItemsService.findAll();

        assertThat(list).hasSize(1);
        ServiceItemsResponseVM vm = list.get(0);
        assertThat(vm.id()).isEqualTo(100L);
        assertThat(vm.serviceId()).isEqualTo(10L);
        assertThat(vm.type()).isEqualTo(ServiceItemsType.DEPARTMENTS);
        verify(serviceItemsRepository).findAll();
    }

    @Test
    void testFindAll_Paginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceItems> page = new PageImpl<>(List.of(item), pageable, 1);
        when(serviceItemsRepository.findAll(pageable)).thenReturn(page);

        Page<ServiceItemsResponseVM> result = serviceItemsService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(100L);
        verify(serviceItemsRepository).findAll(pageable);
    }

    @Test
    void testFindByServiceId_Paginated() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<ServiceItems> page = new PageImpl<>(List.of(item), pageable, 1);

        when(serviceItemsRepository.findByServiceId(10L, pageable)).thenReturn(page);

        Page<ServiceItemsResponseVM> result = serviceItemsService.findByServiceId(10L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).serviceId()).isEqualTo(10L);
        verify(serviceItemsRepository).findByServiceId(10L, pageable);
    }

    @Test
    void testFindOne_Found() {
        when(serviceItemsRepository.findById(100L)).thenReturn(Optional.of(item));

        var res = serviceItemsService.findOne(100L);

        assertThat(res).isPresent();
        assertThat(res.get()).isEqualTo(item);
        verify(serviceItemsRepository).findById(100L);
    }

    @Test
    void testFindOne_NotFound() {
        when(serviceItemsRepository.findById(999L)).thenReturn(Optional.empty());

        var res = serviceItemsService.findOne(999L);

        assertThat(res).isEmpty();
        verify(serviceItemsRepository).findById(999L);
    }

    @Test
    void testToggleIsActive() {
        when(serviceItemsRepository.findById(100L)).thenReturn(Optional.of(item));
        when(serviceItemsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var toggled = serviceItemsService.toggleIsActive(100L);

        assertThat(toggled).isPresent();
        assertThat(toggled.get().getIsActive()).isFalse();
        verify(serviceItemsRepository).save(any());
    }

    // ====== findSourcesByTypeAndFacility ======

    @Test
    void testFindSourcesByTypeAndFacility_Success_Departments_NoPagination() {
        // service supports DEPARTMENTS only
        when(departmentsRepository.findByFacilityId(5L)).thenReturn(List.of(
                new com.dazzle.asklepios.domain.Department(),
                new com.dazzle.asklepios.domain.Department()
        ));

        List<DepartmentResponseVM> res =
                serviceItemsService.findSourcesByTypeAndFacility(ServiceItemsType.DEPARTMENTS, 5L);

        assertThat(res).hasSize(2);
        verify(departmentsRepository).findByFacilityId(5L);
    }

    @Test
    void testFindSourcesByTypeAndFacility_NullFacility_Throws() {
        assertThrows(ResponseStatusException.class,
                () -> serviceItemsService.findSourcesByTypeAndFacility(ServiceItemsType.DEPARTMENTS, null));
        verifyNoInteractions(departmentsRepository);
    }

    @Test
    void testFindSourcesByTypeAndFacility_UnsupportedType_Throws() {
        // Pass NULL to guarantee "unsupported" (independent of your enum values)
        assertThrows(ResponseStatusException.class,
                () -> serviceItemsService.findSourcesByTypeAndFacility(null, 5L));
        verifyNoInteractions(departmentsRepository);
    }
}
