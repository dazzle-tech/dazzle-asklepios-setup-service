package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.UomGroupRelation;
import com.dazzle.asklepios.repository.UomGroupRelationRepository;
import jakarta.persistence.Entity;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class UomGroupRelationService {

    private static final Logger LOG = LoggerFactory.getLogger(UomGroupRelationService.class);
    private final UomGroupRelationRepository uomGroupRelationRepository;

    public UomGroupRelationService(UomGroupRelationRepository uomGroupRelationRepository) {
        this.uomGroupRelationRepository = uomGroupRelationRepository;
    }

    @CacheEvict(cacheNames = UomGroupRelationRepository.UOMGROUPRELATION, key = "'all'")
    public UomGroupRelation create(UomGroupRelation relation) {
        LOG.debug("Request to create UOM Group Relation : {}", relation);
        relation.setId(null);
        return uomGroupRelationRepository.save(relation);
    }

    @CacheEvict(cacheNames = UomGroupRelationRepository.UOMGROUPRELATION, key = "'all'")
    public Optional<UomGroupRelation> update(Long id, UomGroupRelation relation) {
        LOG.debug("Request to update UOM Group Relation key={} with : {}", id, relation);
        return uomGroupRelationRepository.findById(id).map(existing -> {
            existing.setUomUnitFromKey(relation.getUomUnitFromKey());
            existing.setUomUnitToKey(relation.getUomUnitToKey());
            existing.setRelation(relation.getRelation());
            existing.setUomGroupKey(relation.getUomGroupKey());
            return uomGroupRelationRepository.save(existing);
        });
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = UomGroupRelationRepository.UOMGROUPRELATION, key = "'all'")
    public List<UomGroupRelation> findAll() {
        LOG.debug("Request to get all UOM Group Relations");
        return uomGroupRelationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<UomGroupRelation> findOne(Long id) {
        LOG.debug("Request to get UOM Group Relation : {}", id);
        return uomGroupRelationRepository.findById(id);
    }

    @CacheEvict(cacheNames = UomGroupRelationRepository.UOMGROUPRELATION, key = "'all'")
    public void delete(Long id) {
        LOG.debug("Request to delete UOM Group Relation : {}", id);
        uomGroupRelationRepository.deleteById(id);
    }
}
