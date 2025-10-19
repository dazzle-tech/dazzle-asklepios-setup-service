package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Service;
import com.dazzle.asklepios.domain.ServiceItems;
import com.dazzle.asklepios.domain.enumeration.ServiceItemsType;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.ServiceItemsRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.DepartmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceItemsCreateVM;
import com.dazzle.asklepios.web.rest.vm.ServiceItemsResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceItemsUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional
public class ServiceItemsService {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceItemsService.class);

    public static final String SERVICE_ITEMS = "serviceItems";

    private final ServiceItemsRepository serviceItemsRepository;
    private final ServiceRepository serviceRepository;
    private final DepartmentsRepository departmentsRepository;

    public ServiceItemsService(ServiceItemsRepository serviceItemsRepository,
                               ServiceRepository serviceRepository, DepartmentsRepository departmentsRepository) {
        this.serviceItemsRepository = serviceItemsRepository;
        this.serviceRepository = serviceRepository;
        this.departmentsRepository = departmentsRepository;
    }

    // ---------- Create ----------

    // CREATE (VM -> Entity)
    @CacheEvict(cacheNames = SERVICE_ITEMS, key = "'all'")
    public ServiceItems create(ServiceItemsCreateVM vm) {
        LOG.debug("Request to create ServiceItems : {}", vm);

        // Fetch the required Service entity (since serviceId is @NotNull)
        Service service = serviceRepository.findById(vm.serviceId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Service not found with id " + vm.serviceId()
                ));

        // Build entity from VM
        ServiceItems entity = ServiceItems.builder()
                .type(vm.type())
                .sourceId(vm.sourceId())
                .service(service)
                .createdBy(vm.createdBy())
                .isActive(vm.isActive() != null ? vm.isActive() : Boolean.TRUE)
                .build();

        // Persist
        return serviceItemsRepository.save(entity);
    }


    // ---------- Update (by id) ----------

    @CacheEvict(cacheNames = SERVICE_ITEMS, key = "'all'")
    public Optional<ServiceItems> update(Long id, ServiceItemsUpdateVM vm) {
        LOG.debug("Request to update ServiceItems id={} with {}", id, vm);

        // Ensure target exists
        ServiceItems existing = serviceItemsRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "ServiceItems not found with id " + id, "serviceItems", "notfound"
                ));

        // Ensure Service exists (required)
        Service service = serviceRepository.findById(vm.serviceId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "Service not found with id " + vm.serviceId(), "service", "notfound"
                ));

        // Map mutable fields
        if (vm.type() != null) existing.setType(vm.type());
        if (vm.sourceId() != null) existing.setSourceId(vm.sourceId());
        existing.setService(service);
        if (vm.isActive() != null) existing.setIsActive(vm.isActive());
        if (vm.lastModifiedBy() != null) existing.setLastModifiedBy(vm.lastModifiedBy());
        existing.setLastModifiedDate(Instant.now());

        ServiceItems saved = serviceItemsRepository.save(existing);
        return Optional.of(saved);
    }

    // ---------- Read (no pagination) ----------

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = SERVICE_ITEMS, key = "'all'")
    public List<ServiceItemsResponseVM> findAll() {
        LOG.debug("Request to get all ServiceItems (no pagination)");
        return serviceItemsRepository.findAll()
                .stream()
                .map(ServiceItemsResponseVM::ofEntity)
                .collect(Collectors.toList());
    }

    // ---------- Read (pagination) ----------

    @Transactional(readOnly = true)
    public Page<ServiceItemsResponseVM> findAll(Pageable pageable) {
        LOG.debug("Request to get ServiceItems with pagination: {}", pageable);
        return serviceItemsRepository.findAll(pageable).map(ServiceItemsResponseVM::ofEntity);
    }

    // Common filters like DepartmentService does (by facility/type/name equivalents):
    @Transactional(readOnly = true)
    public Page<ServiceItemsResponseVM> findByServiceId(Long serviceId, Pageable pageable) {
        LOG.debug("Request to get ServiceItems by serviceId={} pageable={}", serviceId, pageable);
        return serviceItemsRepository.findByServiceId(serviceId, pageable).map(ServiceItemsResponseVM::ofEntity);
    }


    // ---------- Single read ----------

    @Transactional(readOnly = true)
    public Optional<ServiceItems> findOne(Long id) {
        LOG.debug("Request to get ServiceItems : {}", id);
        return serviceItemsRepository.findById(id);
    }

    // ---------- Toggle isActive ----------

    @CacheEvict(cacheNames = SERVICE_ITEMS, key = "'all'")
    public Optional<ServiceItems> toggleIsActive(Long id) {
        LOG.debug("Request to toggle ServiceItems isActive id={}", id);
        return serviceItemsRepository.findById(id)
                .map(item -> {
                    item.setIsActive(!Boolean.TRUE.equals(item.getIsActive()));
                    item.setLastModifiedDate(Instant.now());
                    return serviceItemsRepository.save(item);
                });
    }

    // ---------- Delete ----------

    @CacheEvict(cacheNames = SERVICE_ITEMS, key = "'all'")
    public void delete(Long id) {
        LOG.debug("Request to delete ServiceItems : {}", id);
        serviceItemsRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponseVM> findSourcesByTypeAndFacility(ServiceItemsType type, Long facilityId) {
        LOG.debug("Request(findSourcesByTypeAndFacility) type={} facilityId={}", type, facilityId);

        if (facilityId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "facilityId is required");
        }

        // Supported type: DEPARTMENTS only (pass anything else -> 400)
        boolean isDepartmentType = (type == ServiceItemsType.DEPARTMENTS);
        if (!isDepartmentType) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported type: " + type);
        }

        return departmentsRepository.findByFacilityId(facilityId)
                .stream()
                .map(DepartmentResponseVM::ofEntity)
                .toList();
    }
}
