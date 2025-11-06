package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ActiveIngredientIndications;
import com.dazzle.asklepios.domain.ActiveIngredients;
import com.dazzle.asklepios.domain.Icd10Code;
import com.dazzle.asklepios.repository.ActiveIngredientIndicationsRepository;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
import com.dazzle.asklepios.repository.Icd10Repository;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.activeIngredientIndications.ActiveIngredientIndicationsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientIndications.ActiveIngredientIndicationsUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActiveIngredientIndicationsService {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientIndicationsService.class);

    private final ActiveIngredientIndicationsRepository activeIngredientIndicationsRepo;
    private final Icd10Repository icd10CodeRepo;
    private final ActiveIngredientsRepository activeIngredientsRepo;

    private static final String ENTITY_NAME = "ActiveIngredientIndications";

    public ActiveIngredientIndicationsService(ActiveIngredientIndicationsRepository activeIngredientIndicationsRepo, Icd10Repository icd10CodeRepo, ActiveIngredientsRepository activeIngredientsRepo) {
        this.activeIngredientIndicationsRepo = activeIngredientIndicationsRepo;
        this.icd10CodeRepo = icd10CodeRepo;
        this.activeIngredientsRepo = activeIngredientsRepo;
    }

    // 1) create
    public ActiveIngredientIndications create(ActiveIngredientIndicationsCreateVM activeIngredientIndicationsCreateVM) {
        LOG.debug("create active ingredient indication {}", activeIngredientIndicationsCreateVM);
        ActiveIngredientIndications entity = toEntityForCreate(activeIngredientIndicationsCreateVM);
        ActiveIngredientIndications saved = activeIngredientIndicationsRepo.save(entity);
        LOG.debug("create: saved id={}", saved.getId());
        return saved;
    }

    // 2) update
    public ActiveIngredientIndications update(ActiveIngredientIndicationsUpdateVM activeIngredientIndicationsUpdateVM) {
        LOG.debug("update active ingredient indication : {} ", activeIngredientIndicationsUpdateVM);
        ActiveIngredientIndications entity = activeIngredientIndicationsRepo.findById(activeIngredientIndicationsUpdateVM.id())
                .orElseThrow(() -> new NotFoundAlertException("ActiveIngredientIndications not found: " + activeIngredientIndicationsUpdateVM.id(), ENTITY_NAME, "notfound"));
        applyUpdate(entity, activeIngredientIndicationsUpdateVM);
        ActiveIngredientIndications saved = activeIngredientIndicationsRepo.save(entity);
        LOG.debug("update: saved id={}", saved.getId());
        return saved;
    }

    // 4) get by active ingredient id
    @Transactional(readOnly = true)
    public List<ActiveIngredientIndications> getByActiveIngredientId(Long activeIngredientId) {
        LOG.debug("get active ingredient indications by activeIngredientId : q='{}'", activeIngredientId);
        return activeIngredientIndicationsRepo.findByActiveIngredientId(activeIngredientId);
    }

    // 8) delete active ingredient indication
    @Transactional
    public void hardDelete(Long id) {
        LOG.debug("delete active ingredient indication by id : '{}'", id);
        activeIngredientIndicationsRepo.deleteById(id);
    }

    // Helpers

    private ActiveIngredientIndications toEntityForCreate(ActiveIngredientIndicationsCreateVM activeIngredientIndicationsCreateVM) {
        ActiveIngredients activeIngredients = getActiveIngredient(activeIngredientIndicationsCreateVM.activeIngredientId());
        Icd10Code icd10Code = getIcd10Code(activeIngredientIndicationsCreateVM.icd10CodeId());

        return ActiveIngredientIndications.builder()
        .activeIngredient(activeIngredients)
        .icd10Code(icd10Code)
        .dosage(activeIngredientIndicationsCreateVM.dosage())
        .unit(activeIngredientIndicationsCreateVM.unit())
        .isOffLabel(activeIngredientIndicationsCreateVM.isOffLabel()).build();
    }

    private void applyUpdate(ActiveIngredientIndications activeIngredientIndications, ActiveIngredientIndicationsUpdateVM activeIngredientIndicationsUpdateVM) {

        if (activeIngredientIndicationsUpdateVM.activeIngredientId() != null)
            activeIngredientIndications.setActiveIngredient(getActiveIngredient(activeIngredientIndicationsUpdateVM.activeIngredientId()));
        if (activeIngredientIndicationsUpdateVM.icd10CodeId() != null)
            activeIngredientIndications.setIcd10Code(getIcd10Code(activeIngredientIndicationsUpdateVM.icd10CodeId()));
        if (activeIngredientIndicationsUpdateVM.dosage() != null)
            activeIngredientIndications.setDosage(activeIngredientIndicationsUpdateVM.dosage());
        if (activeIngredientIndicationsUpdateVM.unit() != null)
            activeIngredientIndications.setUnit(activeIngredientIndicationsUpdateVM.unit());
        if (activeIngredientIndicationsUpdateVM.isOffLabel() != null)
            activeIngredientIndications.setIsOffLabel(activeIngredientIndicationsUpdateVM.isOffLabel());

    }

    private ActiveIngredients getActiveIngredient(Long id) {
        LOG.debug("resolveCategory for active ingredients: id={}", id);
        return activeIngredientsRepo.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Active ingredient not found: " + id, "ActiveIngredients", "notfound"));
    }

    private Icd10Code getIcd10Code(Long id) {
        LOG.debug("resolveClass for active ingredients: id={}", id);
        return icd10CodeRepo.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("ICD 10 code not found: " + id, "Icd10Code", "notfound"));
    }
}
