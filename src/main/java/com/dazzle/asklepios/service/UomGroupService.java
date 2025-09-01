package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.UomGroup;
import com.dazzle.asklepios.repository.UomGroupRepository;
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
public class UomGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(UomGroupService.class);
    private final UomGroupRepository uomGroupRepository;

    public UomGroupService(UomGroupRepository uomGroupRepository) {
        this.uomGroupRepository = uomGroupRepository;
    }

    @CacheEvict(cacheNames = UomGroupRepository.UOMGROUP, key = "'all'")
    public UomGroup create(UomGroup group) {
        LOG.debug("Request to create UOM Group : {}", group);
        group.setId(null);
        return uomGroupRepository.save(group);
    }

    @CacheEvict(cacheNames = UomGroupRepository.UOMGROUP, key = "'all'")
    public Optional<UomGroup> update(Long id, UomGroup group) {
        LOG.debug("Request to update UOM Group key={} with : {}", id, group);
        return uomGroupRepository.findById(id).map(existing -> {
            existing.setName(group.getName());
            existing.setDescription(group.getDescription());
            existing.setCode(group.getCode());
            return uomGroupRepository.save(existing);
        });
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = UomGroupRepository.UOMGROUP , key = "'all'")
    public List<UomGroup> findAll() {
        LOG.debug("Request to get all UOM Groups");
        return uomGroupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<UomGroup> findOne(Long id) {
        LOG.debug("Request to get UOM Group : {}", id);
        return uomGroupRepository.findById(id);
    }

    @CacheEvict(cacheNames = UomGroupRepository.UOMGROUP , key = "'all'")
    public void delete(Long id) {
        LOG.debug("Request to delete UOM Group : {}", id);
        uomGroupRepository.deleteById(id);
    }
}
