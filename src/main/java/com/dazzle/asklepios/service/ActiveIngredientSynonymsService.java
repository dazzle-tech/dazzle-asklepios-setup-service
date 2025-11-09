package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ActiveIngredientSynonyms;
import com.dazzle.asklepios.domain.ActiveIngredients;
import com.dazzle.asklepios.repository.ActiveIngredientSynonymsRepository;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.activeIngredientSynonyms.ActiveIngredientSynonymsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientSynonyms.ActiveIngredientSynonymsUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActiveIngredientSynonymsService {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientSynonymsService.class);

    private final ActiveIngredientSynonymsRepository activeIngredientSynonymsRepository;
    private final ActiveIngredientsRepository activeIngredientsRepo;

    private static final String ENTITY_NAME = "ActiveIngredientSynonyms";

    public ActiveIngredientSynonymsService(ActiveIngredientSynonymsRepository activeIngredientSynonymsRepository, ActiveIngredientsRepository activeIngredientsRepo) {
        this.activeIngredientSynonymsRepository = activeIngredientSynonymsRepository;
        this.activeIngredientsRepo = activeIngredientsRepo;
    }

    // 1) create
    public ActiveIngredientSynonyms create(ActiveIngredientSynonymsCreateVM activeIngredientSynonymsCreateVM) {
        LOG.debug("create active ingredient synonyms {}", activeIngredientSynonymsCreateVM);
        ActiveIngredientSynonyms entity = toEntityForCreate(activeIngredientSynonymsCreateVM);
        ActiveIngredientSynonyms saved = activeIngredientSynonymsRepository.save(entity);
        LOG.debug("create: saved id={}", saved.getId());
        return saved;
    }

    // 2) update
    public ActiveIngredientSynonyms update(ActiveIngredientSynonymsUpdateVM activeIngredientSynonymsUpdateVM) {
        LOG.debug("update active ingredient synonyms : {} ", activeIngredientSynonymsUpdateVM);
        ActiveIngredientSynonyms entity = activeIngredientSynonymsRepository.findById(activeIngredientSynonymsUpdateVM.id())
                .orElseThrow(() -> new NotFoundAlertException("ActiveIngredientSynonyms not found: " + activeIngredientSynonymsUpdateVM.id(), ENTITY_NAME, "notfound"));
        applyUpdate(entity, activeIngredientSynonymsUpdateVM);
        ActiveIngredientSynonyms saved = activeIngredientSynonymsRepository.save(entity);
        LOG.debug("update: saved id={}", saved.getId());
        return saved;
    }

    // 4) get by active ingredient id
    @Transactional(readOnly = true)
    public List<ActiveIngredientSynonyms> getByActiveIngredientId(Long activeIngredientId) {
        LOG.debug("get active ingredient synonyms by activeIngredientId : q='{}'", activeIngredientId);
        return activeIngredientSynonymsRepository.findByActiveIngredientId(activeIngredientId);
    }

    // 8) delete active ingredient synonyms
    @Transactional
    public void hardDelete(Long id) {
        LOG.debug("delete active ingredient synonyms by id : '{}'", id);
        activeIngredientSynonymsRepository.deleteById(id);
    }

// Helpers

    private ActiveIngredientSynonyms toEntityForCreate(ActiveIngredientSynonymsCreateVM ingredientSynonymsCreateVM) {
        ActiveIngredients activeIngredients = getActiveIngredient(ingredientSynonymsCreateVM.activeIngredientId());

        return ActiveIngredientSynonyms.builder()
                .activeIngredient(activeIngredients)
                .synonym(ingredientSynonymsCreateVM.synonym())
                .build();
    }

    private void applyUpdate(ActiveIngredientSynonyms activeIngredientSynonyms, ActiveIngredientSynonymsUpdateVM activeIngredientSynonymsUpdateVM) {

        if (activeIngredientSynonymsUpdateVM.activeIngredientId() != null)
            activeIngredientSynonyms.setActiveIngredient(getActiveIngredient(activeIngredientSynonymsUpdateVM.activeIngredientId()));
        if (activeIngredientSynonymsUpdateVM.synonym() != null)
            activeIngredientSynonyms.setSynonym(activeIngredientSynonymsUpdateVM.synonym());

    }

    private ActiveIngredients getActiveIngredient(Long id) {
        LOG.debug("getActiveIngredient for active ingredients: id={}", id);
        return activeIngredientsRepo.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Active ingredient not found: " + id, "ActiveIngredients", "notfound"));
    }

}