package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.Instant;
import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class ServiceService {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceService.class);
    private final ServiceRepository serviceRepository;
    private final EntityManager entityManager;

    public ServiceService(ServiceRepository serviceRepository, EntityManager entityManager) {
        this.serviceRepository = serviceRepository;
        this.entityManager = entityManager;
    }


    public ServiceSetup create(Long facilityId, ServiceSetup incoming) {
        LOG.info("[CREATE] Request to create Service for facilityId={} payload={}", facilityId, incoming);

        if (facilityId == null) {
            throw new BadRequestAlertException("Facility id is required", "service", "facility.required");
        }
        if (incoming == null) {
            throw new BadRequestAlertException("Service payload is required", "service", "payload.required");
        }

        ServiceSetup entity = ServiceSetup.builder()
                .name(incoming.getName())
                .abbreviation(incoming.getAbbreviation())
                .code(incoming.getCode())
                .category(incoming.getCategory())
                .price(incoming.getPrice())
                .currency(incoming.getCurrency())
                .isActive(Boolean.TRUE.equals(incoming.getIsActive()))
                .facility(refFacility(facilityId))
                .build();
        try {
            ServiceSetup saved = serviceRepository.saveAndFlush(entity);
            LOG.info("Successfully created service id={} name='{}' for facilityId={}", saved.getId(), saved.getName(), facilityId);
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            Throwable root = getRootCause(constraintException);
            String message = (root != null ? root.getMessage() : constraintException.getMessage()).toLowerCase();

            LOG.error("Database constraint violation while creating service: {}", message, constraintException);

            if (message.contains("uk_service_facility_name") ||
                    message.contains("unique constraint") ||
                    message.contains("duplicate key") ||
                    message.contains("duplicate entry")) {
                throw new BadRequestAlertException(
                        "A service with the same name already exists in this facility.",
                        "service",
                        "unique.facility.name"
                );
            }
            throw new BadRequestAlertException(
                    "Database constraint violated while creating service (check facility, unique name, or required fields).",
                    "service",
                    "db.constraint"
            );
        }

    }

    public Optional<ServiceSetup> update(Long id, Long facilityId, ServiceSetup incoming) {
        LOG.info("[UPDATE] Request to update Service id={} facilityId={} payload={}", id, facilityId, incoming);

        if (incoming == null) {
            throw new BadRequestAlertException("Service payload is required", "service", "payload.required");
        }

        ServiceSetup existing = serviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Service not found with id " + id, "service", "notfound"));
        existing.setName(incoming.getName());
        existing.setCode(incoming.getCode());
        existing.setAbbreviation(incoming.getAbbreviation());
        existing.setCategory(incoming.getCategory());
        existing.setPrice(incoming.getPrice());
        existing.setCurrency(incoming.getCurrency());
        existing.setIsActive(incoming.getIsActive());

        try {
            ServiceSetup updated = serviceRepository.saveAndFlush(existing);
            LOG.info("Successfully updated service id={} (name='{}')", updated.getId(), updated.getName());
            return Optional.of(updated);
        }catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            Throwable root = getRootCause(constraintException);
            String message = (root != null ? root.getMessage() : constraintException.getMessage()).toLowerCase();

            LOG.error("Database constraint violation while creating service: {}", message, constraintException);

            if (message.contains("uk_service_facility_name") ||
                    message.contains("unique constraint") ||
                    message.contains("duplicate key") ||
                    message.contains("duplicate entry")) {
                throw new BadRequestAlertException(
                        "A service with the same name already exists in this facility.",
                        "service",
                        "unique.facility.name"
                );
            }
            throw new BadRequestAlertException(
                    "Database constraint violated while creating service (check facility, unique name, or required fields).",
                    "service",
                    "db.constraint"
            );
        }

    }

    @Transactional(readOnly = true)
    public Page<ServiceSetup> findByFacility(Long facilityId, Pageable pageable) {
        LOG.debug("Fetching paged Services by facilityId={} pageable={}", facilityId, pageable);
        if (facilityId == null) {
            throw new BadRequestAlertException("Facility id is required", "service", "facility.required");
        }
        return serviceRepository.findByFacility_Id(facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ServiceSetup> findAll(Pageable pageable) {
        LOG.debug("Fetching paged Services (no facility filter) pageable={}", pageable);
        return serviceRepository.findAll(pageable);
    }


    @Transactional(readOnly = true)
    public Page<ServiceSetup> findByCategory(ServiceCategory category, Pageable pageable) {
        LOG.debug("Fetching Services by category={} (no facility filter)", category);
        return serviceRepository.findByCategory(category, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ServiceSetup> findByNameContainingIgnoreCase(String name, Pageable pageable) {
        LOG.debug("Fetching Services by name='{}' (no facility filter)", name);
        return serviceRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<ServiceSetup> findOne(Long id) {
        LOG.debug("Fetching single Service by id={}", id);
        return serviceRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<ServiceSetup> findByCodeContainingIgnoreCase(String code, Pageable pageable) {
        LOG.debug("Fetching Services by code='{}' (no facility filter)", code);
        return serviceRepository.findByCodeContainingIgnoreCase(code, pageable);
    }

    public Optional<ServiceSetup> toggleIsActive(Long id) {
        LOG.info("Toggling isActive for Service id={}", id);
        return serviceRepository.findById(id)
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    ServiceSetup saved = serviceRepository.save(entity);
                    LOG.info("Service id={} active status changed to {}", id, saved.getIsActive());
                    return saved;
                });
    }


    private Facility refFacility(Long facilityId) {
        return entityManager.getReference(Facility.class, facilityId);
    }
}
