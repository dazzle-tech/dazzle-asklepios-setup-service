package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ActiveIngredientAdverseEffects;
import com.dazzle.asklepios.domain.ActiveIngredients;
import com.dazzle.asklepios.repository.ActiveIngredientAdverseEffectsRepository;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.activeIngredientAdverseEffects.ActiveIngredientAdverseEffectsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientAdverseEffects.ActiveIngredientAdverseEffectsUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActiveIngredientAdverseEffectsService {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientAdverseEffectsService.class);

    private final ActiveIngredientAdverseEffectsRepository activeIngredientAdverseEffectsRepository;
    private final ActiveIngredientsRepository activeIngredientsRepo;

    private static final String ENTITY_NAME = "ActiveIngredientAdverseEffects";

    public ActiveIngredientAdverseEffectsService(ActiveIngredientAdverseEffectsRepository activeIngredientAdverseEffectsRepository, ActiveIngredientsRepository activeIngredientsRepo) {
        this.activeIngredientAdverseEffectsRepository = activeIngredientAdverseEffectsRepository;
        this.activeIngredientsRepo = activeIngredientsRepo;
    }

    public ActiveIngredientAdverseEffects create(ActiveIngredientAdverseEffectsCreateVM activeIngredientAdverseEffectsCreateVM) {
        LOG.debug("create active ingredient adverse effect {}", activeIngredientAdverseEffectsCreateVM);
        ActiveIngredientAdverseEffects entity = toEntityForCreate(activeIngredientAdverseEffectsCreateVM);
        ActiveIngredientAdverseEffects saved = activeIngredientAdverseEffectsRepository.save(entity);
        LOG.debug("create: saved id={}", saved.getId());
        return saved;
    }

    public ActiveIngredientAdverseEffects update(ActiveIngredientAdverseEffectsUpdateVM activeIngredientAdverseEffectsUpdateVM) {
        LOG.debug("update active ingredient adverse effect : {} ", activeIngredientAdverseEffectsUpdateVM);
        ActiveIngredientAdverseEffects entity = activeIngredientAdverseEffectsRepository.findById(activeIngredientAdverseEffectsUpdateVM.id())
                .orElseThrow(() -> new NotFoundAlertException("ActiveIngredientAdverseEffects not found: " + activeIngredientAdverseEffectsUpdateVM.id(), ENTITY_NAME, "notfound"));
        applyUpdate(entity, activeIngredientAdverseEffectsUpdateVM);
        ActiveIngredientAdverseEffects saved = activeIngredientAdverseEffectsRepository.save(entity);
        LOG.debug("update: saved id={}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ActiveIngredientAdverseEffects> getByActiveIngredientId(Long activeIngredientId) {
        LOG.debug("get active ingredient adverse effect by activeIngredientId : q='{}'", activeIngredientId);
        return activeIngredientAdverseEffectsRepository.findByActiveIngredientId(activeIngredientId);
    }

    @Transactional
    public void hardDelete(Long id) {
        LOG.debug("delete active ingredient adverse effect by id : '{}'", id);
        activeIngredientAdverseEffectsRepository.deleteById(id);
    }

// Helpers

    private ActiveIngredientAdverseEffects toEntityForCreate(ActiveIngredientAdverseEffectsCreateVM ingredientAdverseEffectsCreateVM) {
        ActiveIngredients activeIngredients = getActiveIngredient(ingredientAdverseEffectsCreateVM.activeIngredientId());

        return ActiveIngredientAdverseEffects.builder()
                .activeIngredient(activeIngredients)
                .adverseEffect(ingredientAdverseEffectsCreateVM.adverseEffect())
                .build();
    }

    private void applyUpdate(ActiveIngredientAdverseEffects activeIngredientAdverseEffects, ActiveIngredientAdverseEffectsUpdateVM activeIngredientAdverseEffectsUpdateVM) {

        if (activeIngredientAdverseEffectsUpdateVM.activeIngredientId() != null)
            activeIngredientAdverseEffects.setActiveIngredient(getActiveIngredient(activeIngredientAdverseEffectsUpdateVM.activeIngredientId()));
        if (activeIngredientAdverseEffectsUpdateVM.adverseEffect() != null)
            activeIngredientAdverseEffects.setAdverseEffect(activeIngredientAdverseEffectsUpdateVM.adverseEffect());

    }

    private ActiveIngredients getActiveIngredient(Long id) {
        LOG.debug("getActiveIngredient for active ingredients: id={}", id);
        return activeIngredientsRepo.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Active ingredient not found: " + id, "ActiveIngredients", "notfound"));
    }

}
