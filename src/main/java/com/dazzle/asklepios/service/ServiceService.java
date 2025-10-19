package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Service;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.ServiceCreateVM;
import com.dazzle.asklepios.web.rest.vm.ServiceResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceUpdateVM;
import jakarta.persistence.EntityManager;
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
    private final EntityManager em;

    public ServiceService(ServiceRepository serviceRepository, EntityManager em) {
        this.serviceRepository = serviceRepository;
        this.em = em;
    }

    // ===== CREATE (scoped by facility) =====
    @CacheEvict(cacheNames = SERVICES, key = "'all:' + #facilityId")
    public Service create(Long facilityId, ServiceCreateVM vm) {
        LOG.debug("Request to create Service for facilityId={} : {}", facilityId, vm);

        Service entity = Service.builder()
                .name(vm.name())
                .abbreviation(vm.abbreviation())
                .code(vm.code())
                .category(vm.category())
                .price(vm.price())
                .currency(vm.currency())
                .isActive(vm.isActive())
                .createdBy(vm.createdBy())
                .createdDate(Instant.now())
                .build();

        // اربط الخدمة بالمنشأة
        entity.setFacility(refFacility(facilityId));

        return serviceRepository.save(entity);
    }

    // ===== UPDATE (scoped by facility) =====
    @CacheEvict(cacheNames = SERVICES, key = "'all:' + #facilityId")
    public Optional<Service> update(Long id, Long facilityId, ServiceUpdateVM vm) {
        LOG.debug("Request to update Service id={} for facilityId={} with {}", id, facilityId, vm);

        Service existing = serviceRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Service not found with id " + id, "service", "notfound"));

        // تأكيد أن الخدمة تتبع نفس الـ facility المطلوب
        ensureSameFacility(existing, facilityId);

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

    // ===== READ (no pagination) - scoped =====
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = SERVICES, key = "'all:' + #facilityId")
    public List<ServiceResponseVM> findAll(Long facilityId) {
        LOG.debug("Request to get all Services for facilityId={} (no pagination)", facilityId);
        return serviceRepository.findByFacility_Id(facilityId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(ServiceResponseVM::ofEntity)
                .collect(Collectors.toList());
    }

    // ===== READ (pagination) - scoped =====
    @Transactional(readOnly = true)
    public Page<ServiceResponseVM> findAll(Long facilityId, Pageable pageable) {
        LOG.debug("Request to get Services with pagination for facilityId={}, pageable={}", facilityId, pageable);
        return serviceRepository.findByFacility_Id(facilityId, pageable).map(ServiceResponseVM::ofEntity);
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponseVM> findByCategory(Long facilityId, ServiceCategory category, Pageable pageable) {
        LOG.debug("Request to get Services by Category for facilityId={}, category={}, pageable={}", facilityId, category, pageable);
        return serviceRepository.findByFacility_IdAndCategory(facilityId, category, pageable).map(ServiceResponseVM::ofEntity);
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponseVM> findByCodeContainingIgnoreCase(Long facilityId, String code, Pageable pageable) {
        LOG.debug("Request to get Services by Code for facilityId={}, code='{}', pageable={}", facilityId, code, pageable);
        return serviceRepository.findByFacility_IdAndCodeContainingIgnoreCase(facilityId, code, pageable).map(ServiceResponseVM::ofEntity);
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponseVM> findByNameContainingIgnoreCase(Long facilityId, String name, Pageable pageable) {
        LOG.debug("Request to get Services by Name for facilityId={}, name='{}', pageable={}", facilityId, name, pageable);
        return serviceRepository.findByFacility_IdAndNameContainingIgnoreCase(facilityId, name, pageable).map(ServiceResponseVM::ofEntity);
    }

    // ===== READ single - scoped =====
    @Transactional(readOnly = true)
    public Optional<Service> findOne(Long id, Long facilityId) {
        LOG.debug("Request to get Service id={} for facilityId={}", id, facilityId);
        return serviceRepository.findById(id)
                .filter(svc -> svc.getFacility() != null && svc.getFacility().getId().equals(facilityId));
    }

    // ===== EXISTS (scoped) =====
    @Transactional(readOnly = true)
    public boolean existsByNameIgnoreCase(Long facilityId, String name) {
        LOG.debug("Request to check existence of Service by name (ignore case) for facilityId={}, name={}", facilityId, name);
        return serviceRepository.existsByFacility_IdAndNameIgnoreCase(facilityId, name);
    }

    // ===== TOGGLE isActive (scoped) =====
    @CacheEvict(cacheNames = SERVICES, key = "'all:' + #facilityId")
    public Optional<Service> toggleIsActive(Long id, Long facilityId) {
        LOG.debug("Request to toggle Service isActive id={} for facilityId={}", id, facilityId);
        return serviceRepository.findById(id)
                .filter(svc -> svc.getFacility() != null && svc.getFacility().getId().equals(facilityId))
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    return serviceRepository.save(entity);
                });
    }

    // ===== Helpers =====
    private Facility refFacility(Long facilityId) {
        if (facilityId == null) {
            throw new BadRequestAlertException("Facility id is required", "service", "facilityrequired");
        }
        return em.getReference(Facility.class, facilityId);
    }

    private void ensureSameFacility(Service existing, Long facilityId) {
        if (existing.getFacility() == null || !existing.getFacility().getId().equals(facilityId)) {
            throw new BadRequestAlertException(
                    "Service does not belong to facility id " + facilityId, "service", "facility.mismatch");
        }
    }
}
