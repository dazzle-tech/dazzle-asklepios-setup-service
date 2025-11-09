package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ActiveIngredientDrugInteractions;
import com.dazzle.asklepios.domain.ActiveIngredients;
import com.dazzle.asklepios.repository.ActiveIngredientDrugInteractionsRepository;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.activeIngredientDrugInteractions.ActiveIngredientDrugInteractionsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientDrugInteractions.ActiveIngredientDrugInteractionsUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActiveIngredientDrugInteractionsService {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientDrugInteractionsService.class);

    private final ActiveIngredientDrugInteractionsRepository drugInteractionsRepository;
    private final ActiveIngredientsRepository activeIngredientsRepo;

    private static final String ENTITY_NAME = "ActiveIngredientDrugInteractions";

    public ActiveIngredientDrugInteractionsService(ActiveIngredientDrugInteractionsRepository drugInteractionsRepository, ActiveIngredientsRepository activeIngredientsRepo) {
        this.drugInteractionsRepository = drugInteractionsRepository;
        this.activeIngredientsRepo = activeIngredientsRepo;
    }

    // 1) create
    public ActiveIngredientDrugInteractions create(ActiveIngredientDrugInteractionsCreateVM vm) {
        LOG.debug("create ActiveIngredientDrugInteractions {}", vm);
        ActiveIngredientDrugInteractions entity = toEntityForCreate(vm);
        ActiveIngredientDrugInteractions saved = drugInteractionsRepository.save(entity);
        LOG.debug("create: saved id={}", saved.getId());
        return saved;
    }

    // 2) update
    public ActiveIngredientDrugInteractions update(ActiveIngredientDrugInteractionsUpdateVM vm) {
        LOG.debug("update ActiveIngredientDrugInteractions {}", vm);
        ActiveIngredientDrugInteractions entity = drugInteractionsRepository.findById(vm.id())
                .orElseThrow(() -> new NotFoundAlertException("ActiveIngredientDrugInteractions not found: " + vm.id(), ENTITY_NAME, "notfound"));
        applyUpdate(entity, vm);
        ActiveIngredientDrugInteractions saved = drugInteractionsRepository.save(entity);
        LOG.debug("update: saved id={}", saved.getId());
        return saved;
    }

    // 4) get by active ingredient id
    @Transactional(readOnly = true)
    public List<ActiveIngredientDrugInteractions> getByActiveIngredientId(Long activeIngredientId) {
        LOG.debug("get drug interactions by activeIngredientId={}", activeIngredientId);
        return drugInteractionsRepository.findByActiveIngredientId(activeIngredientId);
    }

    // 5) delete
    @Transactional
    public void hardDelete(Long id) {
        LOG.debug("delete ActiveIngredientDrugInteractions id={}", id);
        drugInteractionsRepository.deleteById(id);
    }

    // Helpers

    private ActiveIngredientDrugInteractions toEntityForCreate(ActiveIngredientDrugInteractionsCreateVM vm) {
        ActiveIngredients activeIngredient = getActiveIngredient(vm.activeIngredientId());
        ActiveIngredients interactedIngredient = getActiveIngredient(vm.interactedIngredientId());

        return ActiveIngredientDrugInteractions.builder()
                .activeIngredient(activeIngredient)
                .interactedActiveIngredient(interactedIngredient)
                .severity(vm.severity())
                .description(vm.description())
                .build();
    }

    private void applyUpdate(ActiveIngredientDrugInteractions entity, ActiveIngredientDrugInteractionsUpdateVM vm) {
        if (vm.activeIngredientId() != null) {
            entity.setActiveIngredient(getActiveIngredient(vm.activeIngredientId()));
        }
        if (vm.interactedIngredientId() != null) {
            entity.setInteractedActiveIngredient(getActiveIngredient(vm.interactedIngredientId()));
        }
        if (vm.severity() != null) {
            entity.setSeverity(vm.severity());
        }
        if (vm.description() != null) {
            entity.setDescription(vm.description());
        }
    }

    private ActiveIngredients getActiveIngredient(Long id) {
        LOG.debug("getActiveIngredient: id={}", id);
        return activeIngredientsRepo.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Active ingredient not found: " + id, "ActiveIngredients", "notfound"));
    }
}
