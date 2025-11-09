package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Language;
import com.dazzle.asklepios.domain.LanguageTranslation;
import com.dazzle.asklepios.domain.MedicationCategories;
import com.dazzle.asklepios.repository.MedicationCategoriesRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedicationCategoriesService {

    private static final Logger LOG = LoggerFactory.getLogger(MedicationCategoriesService.class);
    private final MedicationCategoriesRepository medicationCategoriesRepository;

    // find all medication categories
    @Transactional(readOnly = true)
    public List<MedicationCategories> findAll() {
        LOG.debug("Request to get all Medication categories");
        return medicationCategoriesRepository.findAll();
    }

    // find one medication category
    @Transactional(readOnly = true)
    public Optional<MedicationCategories> findOne(Long id) {
        LOG.debug("Request to get Medication Category : {}", id);
        return medicationCategoriesRepository.findById(id);
    }

    // create medication category
    public MedicationCategories create(MedicationCategories vm) {
        LOG.debug("Request to create MedicationCategories : {}", vm);

        MedicationCategories entity = new MedicationCategories();
        entity.setName(vm.getName());

        try {
            return medicationCategoriesRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Medication Category data", ex);
        }
    }
    // delete medication category
    public boolean delete(Long id) {
        LOG.debug("Request to delete Medication Category : {}", id);
        if (!medicationCategoriesRepository.existsById(id)) {
            return false;
        }

        medicationCategoriesRepository.deleteById(id);
        return true;
    }

    // update medication category
    public Optional<MedicationCategories> update(Long id, MedicationCategories vm) {
        LOG.debug("Request to update MedicationCategory id={} with data: {}", id, vm);

        return medicationCategoriesRepository.findById(id).map(existing -> {
            // Do NOT change langKey here; treat it as immutable identity
            existing.setName(vm.getName());
            MedicationCategories updated = medicationCategoriesRepository.save(existing);
            LOG.debug("MedicationCategory id={} updated successfully", id);
            return updated;
        });
    }
}
