package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.MedicationCategories;
import com.dazzle.asklepios.domain.MedicationCategoriesClass;
import com.dazzle.asklepios.repository.MedicationCategoriesClassRepository;
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
public class MedicationCategoriesClassService {

    private static final Logger LOG = LoggerFactory.getLogger(MedicationCategoriesClassService.class);
    private final MedicationCategoriesClassRepository medicationCategoriesClassRepository;

    // find all medications category classes
    @Transactional(readOnly = true)
    public List<MedicationCategoriesClass> findAll() {
        LOG.debug("Request to get all Medication categories classes");
        return medicationCategoriesClassRepository.findAll();
    }

    // find one medication category class
    @Transactional(readOnly = true)
    public Optional<MedicationCategoriesClass> findOne(Long id) {
        LOG.debug("Request to get Medication Category class: {}", id);
        return medicationCategoriesClassRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<MedicationCategoriesClass> findByNameFilter(String nameFilter) {
        return medicationCategoriesClassRepository.findByNameContainingIgnoreCase(nameFilter);
    }

    public MedicationCategoriesClass create(MedicationCategoriesClass vm) {
        LOG.debug("Request to create MedicationCategoriesClass : {}", vm);

        MedicationCategoriesClass entity = new MedicationCategoriesClass();
        entity.setId(null);
        entity.setName(vm.getName());
        entity.setMedicationCategoriesId(vm.getMedicationCategoriesId());

        try {
            return medicationCategoriesClassRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Medication Category class data", ex);
        }
    }


    // delete medication category class
    public boolean delete(Long id) {
        LOG.debug("Request to delete Medication Category class: {}", id);
        if (!medicationCategoriesClassRepository.existsById(id)) {
            return false;
        }

        medicationCategoriesClassRepository.deleteById(id);
        return true;
    }

    // update medication category
    public Optional<MedicationCategoriesClass> update(Long id, MedicationCategoriesClass vm) {
        LOG.debug("Request to update MedicationCategoriesClass id={} with data: {}", id, vm);

        return medicationCategoriesClassRepository.findById(id).map(existing -> {
            // Do NOT change langKey here; treat it as immutable identity
            existing.setName(vm.getName());
            existing.setMedicationCategoriesId(vm.getMedicationCategoriesId());
            MedicationCategoriesClass updated = medicationCategoriesClassRepository.save(existing);
            LOG.debug("MedicationCategoryClass id={} updated successfully", id);
            return updated;
        });
    }
    // find one medication category class
    @Transactional(readOnly = true)
    public List<MedicationCategoriesClass> findAllByCategoryId(Long id) {
        LOG.debug("Request to get Medication Category class: {}", id);
        return medicationCategoriesClassRepository.findAllByMedicationCategoriesId(id);
    }


}
