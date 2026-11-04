package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ActiveIngredients;
import com.dazzle.asklepios.domain.Pharmaceutical;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
import com.dazzle.asklepios.repository.PharmaceuticalRepository;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.pharmaceutical.PharmaceuticalCreateVM;
import com.dazzle.asklepios.web.rest.vm.pharmaceutical.PharmaceuticalUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PharmaceuticalService {

    private static final Logger LOG = LoggerFactory.getLogger(PharmaceuticalService.class);

    private static final String ENTITY_NAME = "Pharmaceutical";

    private final PharmaceuticalRepository pharmaceuticalRepository;
    private final ActiveIngredientsRepository activeIngredientsRepository;

    public PharmaceuticalService(PharmaceuticalRepository pharmaceuticalRepository, ActiveIngredientsRepository activeIngredientsRepository) {
        this.pharmaceuticalRepository = pharmaceuticalRepository;
        this.activeIngredientsRepository = activeIngredientsRepository;
    }

    public Pharmaceutical create(PharmaceuticalCreateVM vm) {
        LOG.debug("create pharmaceutical {}", vm);
        Pharmaceutical entity = Pharmaceutical.builder()
                .activeIngredient(getActiveIngredient(vm.activeIngredientId()))
                .pharmaceuticalName(vm.pharmaceuticalName().trim())
                .code(trimToNull(vm.code()))
                .billingCode(trimToNull(vm.billingCode()))
                .itemDescription(trimToNull(vm.itemDescription()))
                .secondDescription(trimToNull(vm.secondDescription()))
                .isActive(vm.isActive() == null || vm.isActive())
                .build();
        Pharmaceutical saved = pharmaceuticalRepository.save(entity);
        LOG.debug("create: saved id={}", saved.getId());
        return saved;
    }

    public Pharmaceutical update(PharmaceuticalUpdateVM vm) {
        LOG.debug("update pharmaceutical {}", vm);
        Pharmaceutical entity = getById(vm.id());
        entity.setPharmaceuticalName(vm.pharmaceuticalName().trim());
        entity.setCode(trimToNull(vm.code()));
        entity.setBillingCode(trimToNull(vm.billingCode()));
        entity.setItemDescription(trimToNull(vm.itemDescription()));
        entity.setSecondDescription(trimToNull(vm.secondDescription()));
        if (vm.isActive() != null) {
            entity.setIsActive(vm.isActive());
        }
        Pharmaceutical saved = pharmaceuticalRepository.save(entity);
        LOG.debug("update: saved id={}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Pharmaceutical getById(Long id) {
        LOG.debug("get pharmaceutical by id={}", id);
        return pharmaceuticalRepository.findWithActiveIngredientById(id)
                .orElseThrow(() -> new NotFoundAlertException("Pharmaceutical not found: " + id, ENTITY_NAME, "notfound"));
    }

    @Transactional(readOnly = true)
    public List<Pharmaceutical> getByActiveIngredientId(Long activeIngredientId) {
        LOG.debug("get pharmaceuticals by activeIngredientId={}", activeIngredientId);
        return pharmaceuticalRepository.findByActiveIngredientIdOrderByIdAsc(activeIngredientId);
    }

    /**
     * Pharmaceuticals by ids, including inactive ones, so existing prescriptions can still show their names.
     */
    @Transactional(readOnly = true)
    public List<Pharmaceutical> getByIds(List<Long> ids) {
        LOG.debug("get pharmaceuticals by ids={}", ids);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return pharmaceuticalRepository.findWithActiveIngredientByIdIn(ids);
    }

    /**
     * Active pharmaceuticals for the prescription picker, filtered by pharmaceutical name or ingredient name.
     */
    @Transactional(readOnly = true)
    public List<Pharmaceutical> searchActive(String search) {
        LOG.debug("search active pharmaceuticals search='{}'", search);
        return pharmaceuticalRepository.searchActive(search == null ? "" : search.trim());
    }

    private ActiveIngredients getActiveIngredient(Long id) {
        return activeIngredientsRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Active ingredient not found: " + id, "ActiveIngredients", "notfound"));
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
