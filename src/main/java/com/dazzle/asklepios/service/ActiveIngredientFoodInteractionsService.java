package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ActiveIngredientFoodInteractions;
import com.dazzle.asklepios.domain.ActiveIngredients;
import com.dazzle.asklepios.repository.ActiveIngredientFoodInteractionsRepository;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.activeIngredientFoodInteractions.ActiveIngredientFoodInteractionsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientFoodInteractions.ActiveIngredientFoodInteractionsUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActiveIngredientFoodInteractionsService {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientFoodInteractionsService.class);

    private final ActiveIngredientFoodInteractionsRepository foodInteractionsRepository;
    private final ActiveIngredientsRepository activeIngredientsRepo;

    private static final String ENTITY_NAME = "ActiveIngredientFoodInteractions";

    public ActiveIngredientFoodInteractionsService(ActiveIngredientFoodInteractionsRepository foodInteractionsRepository, ActiveIngredientsRepository activeIngredientsRepo) {
        this.foodInteractionsRepository = foodInteractionsRepository;
        this.activeIngredientsRepo = activeIngredientsRepo;
    }

    // 1) create
    public ActiveIngredientFoodInteractions create(ActiveIngredientFoodInteractionsCreateVM vm) {
        LOG.debug("create ActiveIngredientFoodInteractions {}", vm);
        ActiveIngredientFoodInteractions entity = toEntityForCreate(vm);
        ActiveIngredientFoodInteractions saved = foodInteractionsRepository.save(entity);
        LOG.debug("create: saved id={}", saved.getId());
        return saved;
    }

    // 2) update
    public ActiveIngredientFoodInteractions update(ActiveIngredientFoodInteractionsUpdateVM vm) {
        LOG.debug("update ActiveIngredientFoodInteractions {}", vm);
        ActiveIngredientFoodInteractions entity = foodInteractionsRepository.findById(vm.id())
                .orElseThrow(() -> new NotFoundAlertException("ActiveIngredientFoodInteractions not found: " + vm.id(), ENTITY_NAME, "notfound"));
        applyUpdate(entity, vm);
        ActiveIngredientFoodInteractions saved = foodInteractionsRepository.save(entity);
        LOG.debug("update: saved id={}", saved.getId());
        return saved;
    }

    // 4) get by active ingredient id
    @Transactional(readOnly = true)
    public List<ActiveIngredientFoodInteractions> getByActiveIngredientId(Long activeIngredientId) {
        LOG.debug("get food interactions by activeIngredientId={}", activeIngredientId);
        return foodInteractionsRepository.findByActiveIngredientId(activeIngredientId);
    }

    // 8) delete
    @Transactional
    public void hardDelete(Long id) {
        LOG.debug("delete ActiveIngredientFoodInteractions id={}", id);
        foodInteractionsRepository.deleteById(id);
    }

    // Helpers

    private ActiveIngredientFoodInteractions toEntityForCreate(ActiveIngredientFoodInteractionsCreateVM vm) {
        ActiveIngredients activeIngredient = getActiveIngredient(vm.activeIngredientId());
        return ActiveIngredientFoodInteractions.builder()
                .activeIngredient(activeIngredient)
                .food(vm.food())
                .severity(vm.severity())
                .description(vm.description())
                .build();
    }

    private void applyUpdate(ActiveIngredientFoodInteractions entity, ActiveIngredientFoodInteractionsUpdateVM vm) {
        if (vm.activeIngredientId() != null) {
            entity.setActiveIngredient(getActiveIngredient(vm.activeIngredientId()));
        }
        if (vm.food() != null) {
            entity.setFood(vm.food());
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
