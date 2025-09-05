package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.repository.FacilityRepository;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FacilityService {

    private static final Logger LOG = LoggerFactory.getLogger(FacilityService.class);

    private final FacilityRepository facilityRepository;

    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    @CacheEvict(cacheNames = FacilityRepository.FACILITIES, key = "'all'")
    public Facility create(Facility facility) {
        LOG.debug("Request to create Facility : {}", facility);
        facility.setId(null); // ensure a new entity
        return facilityRepository.save(facility);
    }

    @CacheEvict(cacheNames = FacilityRepository.FACILITIES, key = "'all'")
    public Optional<Facility> update(Long id, Facility facility) {
        LOG.debug("Request to update Facility id={} with : {}", id, facility);
        return facilityRepository
            .findById(id)
            .map(existing -> {
                existing.setName(facility.getName());
                existing.setType(facility.getType());
                return facilityRepository.save(existing);
            });
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = FacilityRepository.FACILITIES, key = "'all'")
    public List<Facility> findAll() {
        LOG.debug("Request to get all Facilities");
        return facilityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Facility> findOne(Long id) {
        LOG.debug("Request to get Facility : {}", id);
        return facilityRepository.findById(id);
    }

    @CacheEvict(cacheNames = FacilityRepository.FACILITIES, key = "'all'")
    public boolean delete(Long id) {
        LOG.debug("Request to delete Facility : {}", id);
        if (!facilityRepository.existsById(id)) {
            return false;
        }
        facilityRepository.deleteById(id);
        return true;
    }


}
