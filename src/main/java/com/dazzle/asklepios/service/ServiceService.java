package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Service;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.ServiceCreateVM;
import com.dazzle.asklepios.web.rest.vm.ServiceResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@org.springframework.stereotype.Service
public class ServiceService {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceService.class);
    public static final String SERVICES = "services";

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    // CREATE (VM -> entity)
    @CacheEvict(cacheNames = SERVICES, key = "'all'")
    public Service create(ServiceCreateVM vm) {
        LOG.debug("Request to create Service : {}", vm);

        Service entity = Service.builder()
                .name(vm.name())
                .abbreviation(vm.abbreviation())
                .code(vm.code())
                .category(vm.category())
                .price(vm.price())
                .currency(vm.currency())
                .isActive(vm.isActive())
                .createdBy(vm.createdBy())
                .build();

        return serviceRepository.save(entity);
    }

    // UPDATE (VM -> entity)
    @CacheEvict(cacheNames = SERVICES, key = "'all'")
    public Optional<Service> update(Long id, ServiceUpdateVM vm) {
        LOG.debug("Request to update Service id={} with {}", id, vm);

        Service existing = serviceRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Service not found with id " + id, "service", "notfound"));

        if (vm.name() != null) existing.setName(vm.name());
        if (vm.abbreviation() != null) existing.setAbbreviation(vm.abbreviation());
        if (vm.code() != null) existing.setCode(vm.code());
        if (vm.category() != null) existing.setCategory(vm.category());
        if (vm.price() != null) existing.setPrice(vm.price());
        if (vm.currency() != null) existing.setCurrency(vm.currency());
        if (vm.isActive() != null) existing.setIsActive(vm.isActive());
        if (vm.lastModifiedBy() != null) existing.setLastModifiedBy(vm.lastModifiedBy());

        existing.setLastModifiedDate(Instant.now());

        Service updated = serviceRepository.save(existing);
        return Optional.of(updated);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = SERVICES, key = "'all'")
    public List<ServiceResponseVM> findAll() {
        LOG.debug("Request to get all Services (no pagination)");
        return serviceRepository.findAll()
                .stream()
                .map(ServiceResponseVM::ofEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponseVM> findAll(Pageable pageable) {
        LOG.debug("Request to get Services with pagination: {}", pageable);
        return serviceRepository.findAll(pageable).map(ServiceResponseVM::ofEntity);
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponseVM> findByCategory(ServiceCategory category, Pageable pageable) {
        LOG.debug("Request to get Services by Category with pagination category={} pageable={}", category, pageable);
        return serviceRepository.findByCategory(category, pageable).map(ServiceResponseVM::ofEntity);
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponseVM> findByCodeContainingIgnoreCase(String code, Pageable pageable) {
        LOG.debug("Request to get Services by Code with pagination code='{}' pageable={}", code, pageable);
        return serviceRepository.findByCodeContainingIgnoreCase(code, pageable).map(ServiceResponseVM::ofEntity);
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponseVM> findByNameContainingIgnoreCase(String name, Pageable pageable) {
        LOG.debug("Request to get Services by Name with pagination name='{}' pageable={}", name, pageable);
        return serviceRepository.findByNameContainingIgnoreCase(name, pageable).map(ServiceResponseVM::ofEntity);
    }

    @Transactional(readOnly = true)
    public Optional<Service> findOne(Long id) {
        LOG.debug("Request to get Service : {}", id);
        return serviceRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByNameIgnoreCase(String name) {
        LOG.debug("Request to check existence of Service by name (ignore case): {}", name);
        return serviceRepository.existsByNameIgnoreCase(name);
    }

    @CacheEvict(cacheNames = SERVICES, key = "'all'")
    public Optional<Service> toggleIsActive(Long id) {
        LOG.debug("Request to toggle Service isActive id={}", id);
        return serviceRepository.findById(id)
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    return serviceRepository.save(entity);
                });
    }
}
