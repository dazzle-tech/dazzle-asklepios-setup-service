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
import java.util.List;
import java.util.Optional;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class ServiceService {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceService.class);
    private final ServiceRepository serviceRepository;
    private final EntityManager em;

    public ServiceService(ServiceRepository serviceRepository, EntityManager em) {
        this.serviceRepository = serviceRepository;
        this.em = em;
    }

    // ====================== CREATE  ======================
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
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage()).toLowerCase();

            LOG.error("Database constraint violation while creating service: {}", message, ex);

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
        existing.setAbbreviation(incoming.getAbbreviation());
        existing.setCode(incoming.getCode());
        existing.setCategory(incoming.getCategory());
        existing.setPrice(incoming.getPrice());
        existing.setCurrency(incoming.getCurrency());
        existing.setIsActive(incoming.getIsActive());

        try {
            ServiceSetup updated = serviceRepository.saveAndFlush(existing);
            LOG.info("Successfully updated service id={} (name='{}')", updated.getId(), updated.getName());
            return Optional.of(updated);
        }catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage()).toLowerCase();

            LOG.error("Database constraint violation while creating service: {}", message, ex);

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

    // ====================== READ ======================
    @Transactional(readOnly = true)
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
}
