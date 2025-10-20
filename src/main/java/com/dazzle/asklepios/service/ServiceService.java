package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Service;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
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

    // ===== CREATE (scoped by facility) =====
    @CacheEvict(cacheNames = SERVICES, key = "'all:' + #facilityId")
    public Service create(Long facilityId, Service input) {
        LOG.debug("Request to create Service for facilityId={} : {}", facilityId, input);

        if (facilityId == null) {
            throw new BadRequestAlertException("Facility id is required", "service", "facilityrequired");
        }
        if (input == null) {
            throw new BadRequestAlertException("Service payload is required", "service", "payload.required");
        }

        Service entity = Service.builder()
                .name(input.getName())
                .abbreviation(input.getAbbreviation())
                .code(input.getCode())
                .category(input.getCategory())
                .price(input.getPrice())
                .currency(input.getCurrency())
                .isActive(Boolean.TRUE.equals(input.getIsActive()))
                .build();

        entity.setFacility(refFacility(facilityId));

        Service saved = serviceRepository.save(entity);
        LOG.debug("Created service: {}", saved);
        return saved;
    }

    // ===== UPDATE (scoped by facility) =====
    @CacheEvict(cacheNames = SERVICES, key = "'all:' + #facilityId")
    public Optional<Service> update(Long id, Long facilityId, Service patch) {
        LOG.debug("Request to update Service id={} for facilityId={} with {}", id, facilityId, patch);

        Service existing = serviceRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Service not found with id " + id, "service", "notfound"));

        ensureSameFacility(existing, facilityId);

        if (patch.getName() != null) existing.setName(patch.getName());
        if (patch.getAbbreviation() != null) existing.setAbbreviation(patch.getAbbreviation());
        if (patch.getCode() != null) existing.setCode(patch.getCode());
        if (patch.getCategory() != null) existing.setCategory(patch.getCategory());
        if (patch.getPrice() != null) existing.setPrice(patch.getPrice());
        if (patch.getCurrency() != null) existing.setCurrency(patch.getCurrency());
        if (patch.getIsActive() != null) existing.setIsActive(patch.getIsActive());
        if (patch.getLastModifiedBy() != null) existing.setLastModifiedBy(patch.getLastModifiedBy());

        existing.setLastModifiedDate(Instant.now());

        Service updated = serviceRepository.save(existing);
        LOG.debug("Updated service: {}", updated);
        return Optional.of(updated);
    }

    // ===== READ (no pagination) - scoped =====
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = SERVICES, key = "'all:' + #facilityId")
    public List<Service> findAll(Long facilityId) {
        LOG.debug("Request to get all Services for facilityId={} (no pagination)", facilityId);
        return serviceRepository.findByFacility_Id(facilityId, Pageable.unpaged()).getContent();
    }

    // ===== READ (pagination) - scoped =====
    @Transactional(readOnly = true)
    public Page<Service> findAll(Long facilityId, Pageable pageable) {
        LOG.debug("Request to get Services with pagination for facilityId={}, pageable={}", facilityId, pageable);
        return serviceRepository.findByFacility_Id(facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Service> findByCategory(Long facilityId, ServiceCategory category, Pageable pageable) {
        LOG.debug("Request to get Services by Category for facilityId={}, category={}, pageable={}", facilityId, category, pageable);
        return serviceRepository.findByFacility_IdAndCategory(facilityId, category, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Service> findByCodeContainingIgnoreCase(Long facilityId, String code, Pageable pageable) {
        LOG.debug("Request to get Services by Code for facilityId={}, code='{}', pageable={}", facilityId, code, pageable);
        return serviceRepository.findByFacility_IdAndCodeContainingIgnoreCase(facilityId, code, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Service> findByNameContainingIgnoreCase(Long facilityId, String name, Pageable pageable) {
        LOG.debug("Request to get Services by Name for facilityId={}, name='{}', pageable={}", facilityId, name, pageable);
        return serviceRepository.findByFacility_IdAndNameContainingIgnoreCase(facilityId, name, pageable);
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
        return em.getReference(Facility.class, facilityId);
    }

    private void ensureSameFacility(Service existing, Long facilityId) {
        if (facilityId == null) {
            throw new BadRequestAlertException("Facility id is required", "service", "facilityrequired");
        }
        if (existing.getFacility() == null || !existing.getFacility().getId().equals(facilityId)) {
            throw new BadRequestAlertException(
                    "Service does not belong to facility id " + facilityId, "service", "facility.mismatch");
        }
    }
}
