package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ActiveIngredients;
import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.BrandMedicationActiveIngredient;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
import com.dazzle.asklepios.repository.BrandMedicationActiveIngredientRepository;
import com.dazzle.asklepios.repository.BrandMedicationRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.brandMedicationActiveIngredient.BrandMedicationActiveIngredientCreateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BrandMedicationActiveIngredientService {

    private static final Logger LOG = LoggerFactory.getLogger(BrandMedicationActiveIngredientService.class);

    private final BrandMedicationActiveIngredientRepository brandMedicationActiveIngredientRepository;
    private final BrandMedicationRepository brandRepository;
    private final ActiveIngredientsRepository activeIngredientsRepository;

    public BrandMedicationActiveIngredientService(
            BrandMedicationActiveIngredientRepository brandMedicationActiveIngredientRepository,
            BrandMedicationRepository brandRepository, ActiveIngredientsRepository activeIngredientsRepository
    ) {
        this.brandMedicationActiveIngredientRepository = brandMedicationActiveIngredientRepository;
        this.brandRepository = brandRepository;
        this.activeIngredientsRepository = activeIngredientsRepository;
    }

    public BrandMedicationActiveIngredient create(BrandMedicationActiveIngredientCreateVM vm) {
        LOG.debug("Request to create BrandMedicationActiveIngredient : {}", vm);

        BrandMedication brand = brandRepository.findById(vm.brandId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "BrandMedication not found with id " + vm.brandId(),
                        "brandMedication",
                        "notfound"
                ));

        ActiveIngredients activeIngredients = activeIngredientsRepository.findById(vm.activeIngredientId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "Active Ingredient not found with id " + vm.activeIngredientId(),
                        "activeIngredient",
                        "notfound"
                ));

        // prevent duplicates in either direction
        boolean exists = brandMedicationActiveIngredientRepository.findAllByBrandMedicationId(vm.brandId()).stream()
                .anyMatch(bms ->
                        (bms.getBrandMedication().getId().equals(vm.brandId()) &&
                                bms.getActiveIngredients().getId().equals(vm.activeIngredientId()))
                );

        if (exists) {
            throw new BadRequestAlertException(
                    "Active ingredient relation already exists for the given brand pair",
                    "brandMedicationActiveIngredient",
                    "duplicate"
            );
        }

        BrandMedicationActiveIngredient entity = BrandMedicationActiveIngredient.builder()
                .brandMedication(brand)
                .activeIngredients(activeIngredients)
                .strength(vm.strength())
                .unit(vm.unit())
                .build();

        BrandMedicationActiveIngredient created = brandMedicationActiveIngredientRepository.save(entity);
        LOG.debug("Created BrandMedicationActiveIngredient: {}", created);
        return created;
    }

    @Transactional(readOnly = true)
    public List<BrandMedicationActiveIngredient> findAllByBrandMedication(Long brandMedicationId) {
        LOG.debug("Request to get BrandMedication by brand brandMedicationId={}", brandMedicationId);
        return brandMedicationActiveIngredientRepository.findAllByBrandMedicationId(brandMedicationId);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete BrandMedicationActiveIngredient : {}", id);
        if (!brandMedicationActiveIngredientRepository.existsById(id)) {
            throw new BadRequestAlertException(
                    "BrandMedicationActiveIngredient not found with id " + id,
                    "brandMedicationActiveIngredient",
                    "notfound"
            );
        }
        brandMedicationActiveIngredientRepository.deleteById(id);
    }
    @Transactional(readOnly = true)
    public boolean existsByBrandMedication(Long brandId) {
        return brandMedicationActiveIngredientRepository.existsByBrandMedicationId(brandId);
    }
}
