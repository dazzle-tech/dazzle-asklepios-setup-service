package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.UomGroupUnit;
import com.dazzle.asklepios.repository.UomGroupUnitRepository;
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
public class UomGroupUnitService {

    private static final Logger LOG = LoggerFactory.getLogger(UomGroupUnitService.class);
    private final UomGroupUnitRepository uomGroupUnitRepository;

    public UomGroupUnitService(UomGroupUnitRepository uomGroupUnitRepository) {
        this.uomGroupUnitRepository = uomGroupUnitRepository;
    }

    @CacheEvict(cacheNames = UomGroupUnitRepository.UOMGROUPUNIT, key = "'all'")
    public UomGroupUnit create(UomGroupUnit unit) {
        LOG.debug("Request to create UOM Group Unit : {}", unit);
        unit.setId(null);
        return uomGroupUnitRepository.save(unit);
    }

    @CacheEvict(cacheNames = UomGroupUnitRepository.UOMGROUPUNIT, key = "'all'")
    public Optional<UomGroupUnit> update(Long id, UomGroupUnit unit) {
        LOG.debug("Request to update UOM Group Unit key={} with : {}", id, unit);
        return uomGroupUnitRepository.findById(id).map(existing -> {
            existing.setUom(unit.getUom());
            existing.setUomOrder(unit.getUomOrder());
            existing.setUomGroupKey(unit.getUomGroupKey());
            return uomGroupUnitRepository.save(existing);
        });
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = UomGroupUnitRepository.UOMGROUPUNIT, key = "'all'")
    public List<UomGroupUnit> findAll() {
        LOG.debug("Request to get all UOM Group Units");
        return uomGroupUnitRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<UomGroupUnit> findOne(Long id) {
        LOG.debug("Request to get UOM Group Unit : {}", id);
        return uomGroupUnitRepository.findById(id);
    }

    @CacheEvict(cacheNames = UomGroupUnitRepository.UOMGROUPUNIT, key = "'all'")
    public void delete(Long id) {
        LOG.debug("Request to delete UOM Group Unit : {}", id);
        uomGroupUnitRepository.deleteById(id);
    }
}
