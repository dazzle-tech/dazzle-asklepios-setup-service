package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Allergens;
import java.util.List;
import java.util.Optional;

import com.dazzle.asklepios.domain.Allergens;
import com.dazzle.asklepios.repository.AllergensRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AllergensService {

    private static final Logger LOG = LoggerFactory.getLogger(AllergensService.class);

    private final AllergensRepository allergenRepository;

    public AllergensService(AllergensRepository allergenRepository) {
        this.allergenRepository = allergenRepository;
    }

    @CacheEvict(cacheNames = AllergensRepository.ALLERGENS, key = "'all'")
    public Allergens create(Allergens allergen) {
        LOG.debug("Request to create Allergen : {}", allergen);
        allergen.setId(null); // ensure a new entity
        return allergenRepository.save(allergen);
    }

    @CacheEvict(cacheNames = AllergensRepository.ALLERGENS, key = "'all'")
    public Optional<Allergens> update(Long id, Allergens allergen) {
        LOG.debug("Request to update Allergen id={} with : {}", id, allergen);
        return allergenRepository
                .findById(id)
                .map(existing -> {
                    existing.setCode(allergen.getCode());
                    existing.setName(allergen.getName());
                    existing.setType(allergen.getType());
                    existing.setIsActive(allergen.getIsActive());
                    existing.setDescription(allergen.getDescription());
                    existing.setLastModifiedBy(allergen.getLastModifiedBy());
                    existing.setLastModifiedDate(allergen.getLastModifiedDate());
                    return allergenRepository.save(existing);
                });
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = AllergensRepository.ALLERGENS, key = "'all'")
    public List<Allergens> findAll() {
        LOG.debug("Request to get all Allergens");
        return allergenRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Allergens> findOne(Long id) {
        LOG.debug("Request to get Allergen : {}", id);
        return allergenRepository.findById(id);
    }

    @CacheEvict(cacheNames = AllergensRepository.ALLERGENS, key = "'all'")
    public void delete(Long id) {
        LOG.debug("Request to delete Allergen : {}", id);
        allergenRepository.deleteById(id);
    }
}
