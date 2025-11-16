package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ActiveIngredientContraindications;
import com.dazzle.asklepios.domain.ActiveIngredients;
import com.dazzle.asklepios.domain.Icd10Code;
import com.dazzle.asklepios.repository.ActiveIngredientContraindicationsRepository;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
import com.dazzle.asklepios.repository.Icd10Repository;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.activeIngredientContraindications.ActiveIngredientContraindicationsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientContraindications.ActiveIngredientContraindicationsUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActiveIngredientContraindicationsService {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientContraindicationsService.class);

    private final ActiveIngredientContraindicationsRepository activeIngredientContraindicationsRepo;
    private final Icd10Repository icd10CodeRepo;
    private final ActiveIngredientsRepository activeIngredientsRepo;

    private static final String ENTITY_NAME = "ActiveIngredientContraindications";

    public ActiveIngredientContraindicationsService(ActiveIngredientContraindicationsRepository activeIngredientContraindicationsRepo, Icd10Repository icd10CodeRepo, ActiveIngredientsRepository activeIngredientsRepo) {
        this.activeIngredientContraindicationsRepo = activeIngredientContraindicationsRepo;
        this.icd10CodeRepo = icd10CodeRepo;
        this.activeIngredientsRepo = activeIngredientsRepo;
    }

    public ActiveIngredientContraindications create(ActiveIngredientContraindicationsCreateVM activeIngredientContraindicationsCreateVM) {
        LOG.debug("create active ingredient contraindication {}", activeIngredientContraindicationsCreateVM);
        ActiveIngredientContraindications entity = toEntityForCreate(activeIngredientContraindicationsCreateVM);
        ActiveIngredientContraindications saved = activeIngredientContraindicationsRepo.save(entity);
        LOG.debug("create: saved id={}", saved.getId());
        return saved;
    }

    public ActiveIngredientContraindications update(ActiveIngredientContraindicationsUpdateVM activeIngredientContraindicationsUpdateVM) {
        LOG.debug("update active ingredient contraindication : {} ", activeIngredientContraindicationsUpdateVM);
        ActiveIngredientContraindications entity = activeIngredientContraindicationsRepo.findById(activeIngredientContraindicationsUpdateVM.id())
                .orElseThrow(() -> new NotFoundAlertException("ActiveIngredientContraindications not found: " + activeIngredientContraindicationsUpdateVM.id(), ENTITY_NAME, "notfound"));
        applyUpdate(entity, activeIngredientContraindicationsUpdateVM);
        ActiveIngredientContraindications saved = activeIngredientContraindicationsRepo.save(entity);
        LOG.debug("update: saved id={}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ActiveIngredientContraindications> getByActiveIngredientId(Long activeIngredientId) {
        LOG.debug("get active ingredient contraindication by activeIngredientId : q='{}'", activeIngredientId);
        return activeIngredientContraindicationsRepo.findByActiveIngredientId(activeIngredientId);
    }

    @Transactional
    public void hardDelete(Long id) {
        LOG.debug("delete active ingredient contraindication by id : '{}'", id);
        activeIngredientContraindicationsRepo.deleteById(id);
    }

// Helpers

    private ActiveIngredientContraindications toEntityForCreate(ActiveIngredientContraindicationsCreateVM activeIngredientContraindicationsCreateVM) {
        ActiveIngredients activeIngredients = getActiveIngredient(activeIngredientContraindicationsCreateVM.activeIngredientId());
        Icd10Code icd10Code = getIcd10Code(activeIngredientContraindicationsCreateVM.icd10CodeId());

        return ActiveIngredientContraindications.builder()
                .activeIngredient(activeIngredients)
                .icd10Code(icd10Code)
                .build();
    }

    private void applyUpdate(ActiveIngredientContraindications activeIngredientContraindications, ActiveIngredientContraindicationsUpdateVM activeIngredientContraindicationsUpdateVM) {

        if (activeIngredientContraindicationsUpdateVM.activeIngredientId() != null)
            activeIngredientContraindications.setActiveIngredient(getActiveIngredient(activeIngredientContraindicationsUpdateVM.activeIngredientId()));
        if (activeIngredientContraindicationsUpdateVM.icd10CodeId() != null)
            activeIngredientContraindications.setIcd10Code(getIcd10Code(activeIngredientContraindicationsUpdateVM.icd10CodeId()));

    }

    private ActiveIngredients getActiveIngredient(Long id) {
        LOG.debug("getActiveIngredient for active ingredients: id={}", id);
        return activeIngredientsRepo.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Active ingredient not found: " + id, "ActiveIngredients", "notfound"));
    }

    private Icd10Code getIcd10Code(Long id) {
        LOG.debug("getIcd10Code for Icd 10 code: id={}", id);
        return icd10CodeRepo.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("ICD 10 code not found: " + id, "Icd10Code", "notfound"));
    }
}