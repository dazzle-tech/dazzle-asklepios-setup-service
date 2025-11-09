package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ActiveIngredientSpecialPopulations;
import com.dazzle.asklepios.domain.ActiveIngredients;
import com.dazzle.asklepios.repository.ActiveIngredientSpecialPopulationsRepository;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.activeIngredientSpecialPopulations.ActiveIngredientSpecialPopulationsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientSpecialPopulations.ActiveIngredientSpecialPopulationsUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActiveIngredientSpecialPopulationsService {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientSpecialPopulationsService.class);

    private final ActiveIngredientSpecialPopulationsRepository specialPopulationsRepository;
    private final ActiveIngredientsRepository activeIngredientsRepo;

    private static final String ENTITY_NAME = "ActiveIngredientSpecialPopulations";

    public ActiveIngredientSpecialPopulationsService(ActiveIngredientSpecialPopulationsRepository specialPopulationsRepository, ActiveIngredientsRepository activeIngredientsRepo) {
        this.specialPopulationsRepository = specialPopulationsRepository;
        this.activeIngredientsRepo = activeIngredientsRepo;
    }

    // 1) create
    public ActiveIngredientSpecialPopulations create(ActiveIngredientSpecialPopulationsCreateVM vm) {
        LOG.debug("create ActiveIngredientSpecialPopulations {}", vm);
        ActiveIngredientSpecialPopulations entity = toEntityForCreate(vm);
        ActiveIngredientSpecialPopulations saved = specialPopulationsRepository.save(entity);
        LOG.debug("create: saved id={}", saved.getId());
        return saved;
    }

    // 2) update
    public ActiveIngredientSpecialPopulations update(ActiveIngredientSpecialPopulationsUpdateVM vm) {
        LOG.debug("update ActiveIngredientSpecialPopulations: {}", vm);
        ActiveIngredientSpecialPopulations entity = specialPopulationsRepository.findById(vm.id())
                .orElseThrow(() -> new NotFoundAlertException("ActiveIngredientSpecialPopulations not found: " + vm.id(), ENTITY_NAME, "notfound"));
        applyUpdate(entity, vm);
        ActiveIngredientSpecialPopulations saved = specialPopulationsRepository.save(entity);
        LOG.debug("update: saved id={}", saved.getId());
        return saved;
    }

    // 4) get by active ingredient id
    @Transactional(readOnly = true)
    public List<ActiveIngredientSpecialPopulations> getByActiveIngredientId(Long activeIngredientId) {
        LOG.debug("get special populations by activeIngredientId={}", activeIngredientId);
        return specialPopulationsRepository.findByActiveIngredientId(activeIngredientId);
    }

    // 8) delete
    @Transactional
    public void hardDelete(Long id) {
        LOG.debug("delete ActiveIngredientSpecialPopulations id={}", id);
        specialPopulationsRepository.deleteById(id);
    }

    // Helpers

    private ActiveIngredientSpecialPopulations toEntityForCreate(ActiveIngredientSpecialPopulationsCreateVM vm) {
        ActiveIngredients activeIngredient = getActiveIngredient(vm.activeIngredientId());

        return ActiveIngredientSpecialPopulations.builder()
                .activeIngredient(activeIngredient)
                .additionalPopulation(vm.additionalPopulation())
                .considerations(vm.considerations())
                .build();
    }

    private void applyUpdate(ActiveIngredientSpecialPopulations entity, ActiveIngredientSpecialPopulationsUpdateVM vm) {

        if (vm.activeIngredientId() != null) {
            entity.setActiveIngredient(getActiveIngredient(vm.activeIngredientId()));
        }
        if (vm.additionalPopulation() != null) {
            entity.setAdditionalPopulation(vm.additionalPopulation());
        }
        if (vm.considerations() != null) {
            entity.setConsiderations(vm.considerations());
        }
    }

    private ActiveIngredients getActiveIngredient(Long id) {
        LOG.debug("getActiveIngredient: id={}", id);
        return activeIngredientsRepo.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Active ingredient not found: " + id, "ActiveIngredients", "notfound"));
    }
}
