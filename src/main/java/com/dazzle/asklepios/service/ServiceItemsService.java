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
public class ServiceItemsService {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceItemsService.class);
    public static final String SERVICE_ITEMS = "serviceItems";

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


    @CacheEvict(cacheNames = SERVICE_ITEMS, key = "'byService:' + #serviceId")
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

        if (input.getType() != null && input.getSourceId() != null) {
            boolean duplicate = serviceItemsRepository.existsByServiceIdAndTypeAndSourceId(
                    serviceId, input.getType(), input.getSourceId()
            );
            if (duplicate) {
                throw new BadRequestAlertException(
                        "ServiceItem already exists for this service (type + sourceId must be unique per service).",
                        "serviceItems",
                        "duplicate"
                );
            }
        }

        ServiceItems entity = ServiceItems.builder()
                .type(input.getType())
                .sourceId(input.getSourceId())
                .isActive(input.getIsActive() != null ? input.getIsActive() : Boolean.TRUE)
                .build();

        entity.setService(service);

        ServiceItems saved = serviceItemsRepository.save(entity);
        LOG.debug("Created ServiceItems: {}", saved);
        return saved;
    }

    @CacheEvict(
            cacheNames = SERVICE_ITEMS,
            key = "'byService:' + (#serviceId != null ? #serviceId : (#patch != null && #patch.service != null ? #patch.service.id : 'unknown'))",
            allEntries = false
    )
    public Optional<ServiceItems> update(Long id, Long serviceId, ServiceItems patch) {
        LOG.debug("Request to update ServiceItems id={} serviceId={} with {}", id, serviceId, patch);

        ServiceItems existing = serviceItemsRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "ServiceItems not found with id " + id, "serviceItems", "notfound"));

        Long targetServiceId = (serviceId != null) ? serviceId
                : (existing.getService() != null ? existing.getService().getId() : null);

        if (targetServiceId == null) {
            throw new BadRequestAlertException("Service ID is required", "serviceItems", "serviceid.required");
        }

        if (serviceId != null) {
            ServiceSetup service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new NotFoundAlertException(
                            "Service not found with id " + serviceId, "service", "notfound"));
            existing.setService(service);
        }

        ServiceItemsType targetType = (patch != null && patch.getType() != null)
                ? patch.getType()
                : existing.getType();

        Long targetSourceId = (patch != null && patch.getSourceId() != null)
                ? patch.getSourceId()
                : existing.getSourceId();

        if (targetType != null && targetSourceId != null) {
            boolean duplicate = serviceItemsRepository.existsByServiceIdAndTypeAndSourceIdAndIdNot(
                    targetServiceId, targetType, targetSourceId, id
            );
            if (duplicate) {
                throw new BadRequestAlertException(
                        "Another ServiceItem with the same (type, sourceId) already exists for this service.",
                        "serviceItems",
                        "duplicate"
                );
            }
        }

        if (patch != null) {
            if (patch.getType() != null) existing.setType(patch.getType());
            if (patch.getSourceId() != null) existing.setSourceId(patch.getSourceId());
            if (patch.getIsActive() != null) existing.setIsActive(patch.getIsActive());
            if (patch.getLastModifiedBy() != null) existing.setLastModifiedBy(patch.getLastModifiedBy());
        }

        existing.setLastModifiedDate(Instant.now());

        ServiceItems updated = serviceItemsRepository.save(existing);
        LOG.debug("Updated ServiceItems: {}", updated);
        return Optional.of(updated);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = SERVICE_ITEMS, key = "'all'")
    public List<ServiceItems> findAll() {
        LOG.debug("Request to get all ServiceItems (no pagination)");
        return serviceItemsRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = SERVICE_ITEMS, key = "'byService:' + #serviceId")
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

    @CacheEvict(cacheNames = SERVICE_ITEMS, key = "'all'")
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

    @CacheEvict(cacheNames = SERVICE_ITEMS, key = "'all'")
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
            return departmentsRepository.findByFacilityId(facilityId);
        }

        LOG.info("Source lookup for type {} is not implemented yet. Returning an empty list.", type);
        return List.of();
    }
}
