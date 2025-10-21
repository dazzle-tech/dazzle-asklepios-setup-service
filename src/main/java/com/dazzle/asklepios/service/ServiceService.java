package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.ServiceCreateVM;
import com.dazzle.asklepios.web.rest.vm.ServiceUpdateVM;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServiceService {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceService.class);
    public static final String SERVICES = "services";

    private final ServiceRepository serviceRepository;
    private final EntityManager em;

    public ServiceService(ServiceRepository serviceRepository, EntityManager em) {
        this.serviceRepository = serviceRepository;
        this.em = em;
    }

    @CacheEvict(cacheNames = SERVICES, key = "'all:' + #facilityId")
    public ServiceSetup createWithDuplicateCheck(Long facilityId, ServiceCreateVM vm) {
        LOG.info("[CREATE] Request to create Service for facilityId={} payload={}", facilityId, vm);

        if (facilityId == null) {
            LOG.error("Facility ID is null");
            throw new BadRequestAlertException("Facility id is required", "service", "facilityrequired");
        }

        if (vm == null) {
            LOG.error("ServiceCreateVM payload is null");
            throw new BadRequestAlertException("Service payload is required", "service", "payload.required");
        }

        // Check for duplicate name
        LOG.debug("Checking for duplicate service name='{}' in facilityId={}", vm.name(), facilityId);
        if (vm.name() != null && serviceRepository.existsByFacility_IdAndNameIgnoreCase(facilityId, vm.name())) {
            LOG.warn("Duplicate service detected: name='{}' already exists in facilityId={}", vm.name(), facilityId);
            throw new BadRequestAlertException(
                    "A service with the same name already exists in this facility.",
                    "service",
                    "duplicate"
            );
        }

        ServiceSetup entity = ServiceSetup.builder()
                .name(vm.name())
                .abbreviation(vm.abbreviation())
                .code(vm.code())
                .category(vm.category())
                .price(vm.price())
                .currency(vm.currency())
                .isActive(Boolean.TRUE.equals(vm.isActive()))
                .build();

        entity.setFacility(refFacility(facilityId));

        LOG.debug("Saving new ServiceSetup entity for facilityId={}", facilityId);
        ServiceSetup saved = serviceRepository.save(entity);

        LOG.info("Successfully created service id={} name='{}' for facilityId={}", saved.getId(), saved.getName(), facilityId);
        return saved;
    }

    @CacheEvict(cacheNames = SERVICES, key = "'all:' + #facilityId")
    public Optional<ServiceSetup> updateWithDuplicateCheck(Long id, Long facilityId, ServiceUpdateVM vm) {
        LOG.info("[UPDATE] Request to update Service id={} facilityId={} payload={}", id, facilityId, vm);

        ServiceSetup existing = serviceRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error("Service not found with id={}", id);
                    return new NotFoundAlertException("Service not found with id " + id, "service", "notfound");
                });

        ensureSameFacility(existing, facilityId);

        // Check for duplicate name
        if (vm.name() != null) {
            LOG.debug("Checking for duplicate name='{}' in facilityId={}", vm.name(), facilityId);
            boolean duplicate = serviceRepository.existsByFacility_IdAndNameIgnoreCase(facilityId, vm.name());
            if (duplicate && !existing.getName().equalsIgnoreCase(vm.name())) {
                LOG.warn("Duplicate service name detected during update: '{}'", vm.name());
                throw new BadRequestAlertException(
                        "Another service with the same name already exists in this facility.",
                        "service",
                        "duplicate"
                );
            }
        }

        // Apply updates
        LOG.debug("Applying updates to Service id={}", id);
        if (vm.name() != null) existing.setName(vm.name());
        if (vm.abbreviation() != null) existing.setAbbreviation(vm.abbreviation());
        if (vm.code() != null) existing.setCode(vm.code());
        if (vm.category() != null) existing.setCategory(vm.category());
        if (vm.price() != null) existing.setPrice(vm.price());
        if (vm.currency() != null) existing.setCurrency(vm.currency());
        if (vm.isActive() != null) existing.setIsActive(vm.isActive());
        if (vm.lastModifiedBy() != null) existing.setLastModifiedBy(vm.lastModifiedBy());

        existing.setLastModifiedDate(Instant.now());

        LOG.debug("Saving updated Service id={}", id);
        ServiceSetup updated = serviceRepository.save(existing);

        LOG.info("Successfully updated service id={} (name='{}')", updated.getId(), updated.getName());
        return Optional.of(updated);
    }

    // ====================== READ ======================
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = SERVICES, key = "'all:' + #facilityId")
    public List<ServiceSetup> findAll(Long facilityId) {
        LOG.debug("Fetching all Services for facilityId={}", facilityId);
        return serviceRepository.findByFacility_Id(facilityId);
    }

    @Transactional(readOnly = true)
    public Page<ServiceSetup> findAll(Long facilityId, Pageable pageable) {
        LOG.debug("Fetching paged Services for facilityId={} pageable={}", facilityId, pageable);
        return serviceRepository.findByFacility_Id(facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ServiceSetup> findByCategory(Long facilityId, ServiceCategory category, Pageable pageable) {
        LOG.debug("Fetching Services by category={} facilityId={}", category, facilityId);
        return serviceRepository.findByFacility_IdAndCategory(facilityId, category, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ServiceSetup> findByCodeContainingIgnoreCase(Long facilityId, String code, Pageable pageable) {
        LOG.debug("Fetching Services by code='{}' facilityId={}", code, facilityId);
        return serviceRepository.findByFacility_IdAndCodeContainingIgnoreCase(facilityId, code, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ServiceSetup> findByNameContainingIgnoreCase(Long facilityId, String name, Pageable pageable) {
        LOG.debug("Fetching Services by name='{}' facilityId={}", name, facilityId);
        return serviceRepository.findByFacility_IdAndNameContainingIgnoreCase(facilityId, name, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<ServiceSetup> findOne(Long id) {
        LOG.debug("Fetching single Service by id={}", id);
        return serviceRepository.findById(id);
    }

    @CacheEvict(cacheNames = SERVICES, key = "'all:' + #facilityId")
    public Optional<ServiceSetup> toggleIsActive(Long id, Long facilityId) {
        LOG.info("Toggling isActive for Service id={} facilityId={}", id, facilityId);
        return serviceRepository.findById(id)
                .filter(svc -> svc.getFacility() != null && svc.getFacility().getId().equals(facilityId))
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    ServiceSetup saved = serviceRepository.save(entity);
                    LOG.info("Service id={} active status changed to {}", id, saved.getIsActive());
                    return saved;
                });
    }

    // ====================== Helpers ======================
    private Facility refFacility(Long facilityId) {
        return em.getReference(Facility.class, facilityId);
    }

    private void ensureSameFacility(ServiceSetup existing, Long facilityId) {
        if (facilityId == null) {
            LOG.error("Facility id is null during facility check");
            throw new BadRequestAlertException("Facility id is required", "service", "facilityrequired");
        }
        if (existing.getFacility() == null || !existing.getFacility().getId().equals(facilityId)) {
            LOG.error("Facility mismatch: existingServiceFacilityId={}, providedFacilityId={}",
                    existing.getFacility() != null ? existing.getFacility().getId() : null, facilityId);
            throw new BadRequestAlertException(
                    "Service does not belong to facility id " + facilityId,
                    "service",
                    "facility.mismatch"
            );
        }
    }
}
