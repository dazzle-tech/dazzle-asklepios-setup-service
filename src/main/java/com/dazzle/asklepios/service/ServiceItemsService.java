package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ServiceItems;
import com.dazzle.asklepios.repository.ServiceItemsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServiceItemsService {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceItemsService.class);

    private final ServiceItemsRepository serviceItemsRepository;

    public static final String SERVICE_ITEMS = "serviceItems";

    public ServiceItemsService(ServiceItemsRepository serviceItemsRepository) {
        this.serviceItemsRepository = serviceItemsRepository;
    }

    @CacheEvict(cacheNames = SERVICE_ITEMS, key = "'all'")
    public ServiceItems create(ServiceItems serviceItems) {
        LOG.debug("Request to create ServiceItems : {}", serviceItems);
        serviceItems.setId(null);
        return serviceItemsRepository.save(serviceItems);
    }

    @CacheEvict(cacheNames = SERVICE_ITEMS, key = "'all'")
    public Optional<ServiceItems> update(Long id, ServiceItems serviceItems) {
        LOG.debug("Request to update ServiceItems id={} with : {}", id, serviceItems);
        return serviceItemsRepository
                .findById(id)
                .map(existing -> {
                    existing.setType(serviceItems.getType());
                    existing.setSourceId(serviceItems.getSourceId());
                    existing.setServiceId(serviceItems.getServiceId());
                    existing.setCreatedBy(serviceItems.getCreatedBy());
                    existing.setCreatedDate(serviceItems.getCreatedDate());
                    existing.setLastModifiedBy(serviceItems.getLastModifiedBy());
                    existing.setLastModifiedDate(serviceItems.getLastModifiedDate());
                    existing.setIsActive(serviceItems.getIsActive());
                    return serviceItemsRepository.save(existing);
                });
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = SERVICE_ITEMS, key = "'all'")
    public List<ServiceItems> findAll() {
        LOG.debug("Request to get all ServiceItems");
        return serviceItemsRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ServiceItems> findOne(Long id) {
        LOG.debug("Request to get ServiceItems : {}", id);
        return serviceItemsRepository.findById(id);
    }

    @CacheEvict(cacheNames = SERVICE_ITEMS, key = "'all'")
    public void delete(Long id) {
        LOG.debug("Request to delete ServiceItems : {}", id);
        serviceItemsRepository.deleteById(id);
    }
}
