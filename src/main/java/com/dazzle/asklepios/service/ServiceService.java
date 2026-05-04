package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.ServiceItems;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.domain.enumeration.ServiceItemsType;
import com.dazzle.asklepios.repository.ServiceItemsRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class ServiceService {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceService.class);

    private final ServiceRepository serviceRepository;
    private final EntityManager entityManager;
    private final ServiceItemsRepository serviceItemsRepository;

    public ServiceService(
            ServiceRepository serviceRepository,
            EntityManager entityManager,
            ServiceItemsRepository serviceItemsRepository
    ) {
        this.serviceRepository = serviceRepository;
        this.entityManager = entityManager;
        this.serviceItemsRepository = serviceItemsRepository;
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
                .isActive(incoming.getIsActive() != null ? incoming.getIsActive() : true)
                .facility(refFacility(facilityId))
                .appointable(incoming.getAppointable())
                .parallelCapacityValue(
                        incoming.getParallelCapacityValue() != null ? incoming.getParallelCapacityValue() : 1
                )
                .defaultDurationMinutes(incoming.getDefaultDurationMinutes())
                .defaultBufferBeforeMinutes(
                        incoming.getDefaultBufferBeforeMinutes() != null ? incoming.getDefaultBufferBeforeMinutes() : 0
                )
                .defaultBufferAfterMinutes(
                        incoming.getDefaultBufferAfterMinutes() != null ? incoming.getDefaultBufferAfterMinutes() : 0
                )
                .build();

        validateAppointableRequirements(entity);

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

        if (facilityId != null) {
            existing.setFacility(refFacility(facilityId));
        }

        existing.setName(incoming.getName());
        existing.setCode(incoming.getCode());
        existing.setAbbreviation(incoming.getAbbreviation());
        existing.setCategory(incoming.getCategory());
        existing.setPrice(incoming.getPrice());
        existing.setCurrency(incoming.getCurrency());
        existing.setIsActive(incoming.getIsActive());
        existing.setAppointable(incoming.getAppointable());

        if (incoming.getParallelCapacityValue() != null) {
            existing.setParallelCapacityValue(incoming.getParallelCapacityValue());
        }
        if (incoming.getDefaultDurationMinutes() != null) {
            existing.setDefaultDurationMinutes(incoming.getDefaultDurationMinutes());
        }
        if (incoming.getDefaultBufferBeforeMinutes() != null) {
            existing.setDefaultBufferBeforeMinutes(incoming.getDefaultBufferBeforeMinutes());
        }
        if (incoming.getDefaultBufferAfterMinutes() != null) {
            existing.setDefaultBufferAfterMinutes(incoming.getDefaultBufferAfterMinutes());
        }

        validateAppointableRequirements(existing);

        try {
            ServiceSetup updated = serviceRepository.saveAndFlush(existing);
            LOG.info("Successfully updated service id={} (name='{}')", updated.getId(), updated.getName());
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            Throwable root = getRootCause(constraintException);
            String message = (root != null ? root.getMessage() : constraintException.getMessage()).toLowerCase();

            LOG.error("Database constraint violation while updating service: {}", message, constraintException);

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
                    "Database constraint violated while updating service (check facility, unique name, or required fields).",
                    "service",
                    "db.constraint"
            );
        }
    }

    private void validateAppointableRequirements(ServiceSetup service) {
        if (Boolean.TRUE.equals(service.getAppointable())) {
            if (service.getDefaultDurationMinutes() == null || service.getDefaultDurationMinutes() <= 0) {
                throw new BadRequestAlertException(
                        "defaultDurationMinutes must be greater than 0 when appointable is true",
                        "service",
                        "defaultdurationinvalid"
                );
            }

            if (service.getDefaultBufferBeforeMinutes() == null || service.getDefaultBufferBeforeMinutes() < 0) {
                throw new BadRequestAlertException(
                        "defaultBufferBeforeMinutes must be 0 or greater when appointable is true",
                        "service",
                        "bufferbeforeinvalid"
                );
            }

            if (service.getDefaultBufferAfterMinutes() == null || service.getDefaultBufferAfterMinutes() < 0) {
                throw new BadRequestAlertException(
                        "defaultBufferAfterMinutes must be 0 or greater when appointable is true",
                        "service",
                        "bufferafterinvalid"
                );
            }
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

    // ServiceService.java
    @Transactional(readOnly = true)
    public Page<ServiceSetup> findServicesByDepartmentSource(Long sourceId, Pageable pageable) {
        LOG.debug("Fetching paged active Services by department sourceId={} pageable={}", sourceId, pageable);

        List<ServiceItems> items =
                serviceItemsRepository.findByTypeAndSourceIdAndIsActiveTrue(
                        ServiceItemsType.DEPARTMENTS,
                        sourceId
                );

        if (items == null || items.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> serviceIds = items.stream()
                .map(serviceItems -> serviceItems.getService().getId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (serviceIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return serviceRepository.findByIdInAndIsActiveTrue(serviceIds, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ServiceSetup> findActiveByFacility(Long facilityId, Pageable pageable) {
        LOG.debug("Fetching active Services by facilityId={} pageable={}", facilityId, pageable);
        return serviceRepository.findByFacility_IdAndIsActiveTrue(facilityId, pageable);
    }

    public List<ServiceSetup> findAllByIds(List<Long> ids) {
        return serviceRepository.findAllById(ids);
    }
    public Page<ServiceSetup> findActiveAppointableBasedOnLoggedInFacility(Pageable pageable) {
        LOG.debug("Fetching Active Appointable  Service pageable={} is", pageable);
        Long facilityId = getFacility();
        return serviceRepository.findByIsActiveTrueAndAppointableTrueAndFacility_Id(facilityId, pageable);
    }

    private Facility refFacility(Long facilityId) {
        return entityManager.getReference(Facility.class, facilityId);
    }

    private Long getFacility(){

        return SecurityUtils.getCurrentUserFacility()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing mandatory claim 'tenant' in JWT."));

    }
}
