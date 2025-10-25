package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.ServiceItems;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.ServiceItemsType;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.ServiceItemsRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class ServiceItemsService {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceItemsService.class);
    private final ServiceItemsRepository serviceItemsRepository;
    private final ServiceRepository serviceRepository;
    private final DepartmentsRepository departmentsRepository;

    public ServiceItemsService(
            ServiceItemsRepository serviceItemsRepository,
            ServiceRepository serviceRepository,
            DepartmentsRepository departmentsRepository
    ) {
        this.serviceItemsRepository = serviceItemsRepository;
        this.serviceRepository = serviceRepository;
        this.departmentsRepository = departmentsRepository;
    }
    public ServiceItems create(Long serviceId, ServiceItems input) {
        LOG.debug("Request to create ServiceItems for serviceId={} payload={}", serviceId, input);

        if (serviceId == null) {
            throw new BadRequestAlertException("Service ID is required", "serviceItems", "serviceid.required");
        }
        if (input == null) {
            throw new BadRequestAlertException("ServiceItems payload is required", "serviceItems", "payload.required");
        }

        ServiceSetup service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Service not found with id " + serviceId, "service", "notfound"));

        ServiceItems entity = ServiceItems.builder()
                .type(input.getType())
                .sourceId(input.getSourceId())
                .isActive(input.getIsActive() != null ? input.getIsActive() : Boolean.TRUE)
                .build();
        entity.setService(service);

        try {
            ServiceItems saved = serviceItemsRepository.saveAndFlush(entity);
            LOG.debug("Created ServiceItems: {}", saved);
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String msg = (root != null && root.getMessage() != null ? root.getMessage() : ex.getMessage()).toLowerCase();

            LOG.warn("DB constraint violation while creating ServiceItems (serviceId={}, type={}, sourceId={}): {}",
                    serviceId, input.getType(), input.getSourceId(), msg, ex);

            if (msg.contains("uk_service_items_service_type_source")
                    || msg.contains("unique constraint")
                    || msg.contains("duplicate key")
                    || msg.contains("duplicate entry")) {
                throw new BadRequestAlertException(
                        "A ServiceItem with the same (type, sourceId) already exists for this service.",
                        "serviceItems",
                        "unique.serviceitem"
                );
            }
            throw new BadRequestAlertException(
                    "Database constraint violated while creating ServiceItem (check unique/type/source/service).",
                    "serviceItems",
                    "db.constraint"
            );
        }
    }

    public Optional<ServiceItems> update(Long id, Long serviceId, ServiceItems patch) {
        LOG.debug("Request to update ServiceItems id={} serviceId={} with {}", id, serviceId, patch);

        ServiceItems existing = serviceItemsRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "ServiceItems not found with id " + id, "serviceItems", "notfound"));

        if (serviceId != null) {
            ServiceSetup service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new NotFoundAlertException(
                            "Service not found with id " + serviceId, "service", "notfound"));
            existing.setService(service);
        }

        try {
            ServiceItems updated = serviceItemsRepository.saveAndFlush(existing);
            LOG.debug("Updated ServiceItems: {}", updated);
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String msg = (root != null && root.getMessage() != null ? root.getMessage() : ex.getMessage()).toLowerCase();

            LOG.warn("DB constraint violation while updating ServiceItems id={} (serviceId={}, type={}, sourceId={}): {}",
                    id,
                    existing.getService() != null ? existing.getService().getId() : null,
                    existing.getType(),
                    existing.getSourceId(),
                    msg,
                    ex
            );

            if (msg.contains("uk_service_items_service_type_source")
                    || msg.contains("unique constraint")
                    || msg.contains("duplicate key")
                    || msg.contains("duplicate entry")) {
                throw new BadRequestAlertException(
                        "Another ServiceItem with the same (type, sourceId) already exists for this service.",
                        "serviceItems",
                        "unique.serviceitem"
                );
            }

            throw new BadRequestAlertException(
                    "Database constraint violated while updating ServiceItem (check unique/type/source/service).",
                    "serviceItems",
                    "db.constraint"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ServiceItems> findAll() {
        LOG.debug("Request to get all ServiceItems (no pagination)");
        return serviceItemsRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ServiceItems> findByServiceId(Long serviceId) {
        LOG.debug("Request to get ServiceItems by serviceId={} (no pagination)", serviceId);

        if (serviceId == null) {
            throw new BadRequestAlertException("Service ID is required", "serviceItems", "serviceid.required");
        }
        return serviceItemsRepository.findByServiceId(serviceId);
    }

    @Transactional(readOnly = true)
    public Page<ServiceItems> findAll(Pageable pageable) {
        LOG.debug("Request to get ServiceItems with pagination: {}", pageable);
        return serviceItemsRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ServiceItems> findByServiceId(Long serviceId, Pageable pageable) {
        LOG.debug("Request to get ServiceItems by serviceId={} pageable={}", serviceId, pageable);

        if (serviceId == null) {
            throw new BadRequestAlertException("Service ID is required", "serviceItems", "serviceid.required");
        }
        return serviceItemsRepository.findByServiceId(serviceId, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<ServiceItems> findOne(Long id) {
        LOG.debug("Request to get ServiceItems : {}", id);
        return serviceItemsRepository.findById(id);
    }

    public Optional<ServiceItems> toggleIsActive(Long id) {
        LOG.debug("Request to toggle ServiceItems isActive id={}", id);

        ServiceItems item = serviceItemsRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "ServiceItems not found with id " + id, "serviceItems", "notfound"));

        item.setIsActive(!Boolean.TRUE.equals(item.getIsActive()));
        item.setLastModifiedDate(Instant.now());
        ServiceItems saved = serviceItemsRepository.save(item);

        LOG.debug("Toggled ServiceItems isActive for id={}", id);
        return Optional.of(saved);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete ServiceItems : {}", id);

        if (!serviceItemsRepository.existsById(id)) {
            throw new NotFoundAlertException("ServiceItems not found with id " + id, "serviceItems", "notfound");
        }

        serviceItemsRepository.deleteById(id);
        LOG.debug("Deleted ServiceItems id={}", id);
    }

    @Transactional(readOnly = true)
    public List<Department> findSourcesByTypeAndFacility(ServiceItemsType type, Long facilityId) {
        LOG.debug("Request(findSourcesByTypeAndFacility) type={} facilityId={}", type, facilityId);

        if (facilityId == null) {
            throw new BadRequestAlertException("Facility ID is required", "serviceItems", "facilityid.required");
        }

        if (type == ServiceItemsType.DEPARTMENTS) {
            return departmentsRepository.findByFacilityIdAndIsActiveTrue(facilityId);
        }

        LOG.info("Source lookup for type {} is not implemented yet. Returning an empty list.", type);
        return List.of();
    }
}
