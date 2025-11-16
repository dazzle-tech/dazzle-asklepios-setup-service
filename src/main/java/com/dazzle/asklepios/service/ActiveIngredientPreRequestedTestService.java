package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ActiveIngredientPreRequestedTest;
import com.dazzle.asklepios.domain.ActiveIngredients;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.repository.ActiveIngredientPreRequestedTestRepository;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.activeIngredientPreRequestedTest.ActiveIngredientPreRequestedTestCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientPreRequestedTest.ActiveIngredientPreRequestedTestUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActiveIngredientPreRequestedTestService {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientPreRequestedTestService.class);

    private final ActiveIngredientPreRequestedTestRepository preRequestedTestRepository;
    private final ActiveIngredientsRepository activeIngredientsRepo;
    private final DiagnosticTestRepository diagnosticTestRepository;

    private static final String ENTITY_NAME = "ActiveIngredientPreRequestedTest";

    public ActiveIngredientPreRequestedTestService(ActiveIngredientPreRequestedTestRepository preRequestedTestRepository, ActiveIngredientsRepository activeIngredientsRepo, DiagnosticTestRepository diagnosticTestRepository) {
        this.preRequestedTestRepository = preRequestedTestRepository;
        this.activeIngredientsRepo = activeIngredientsRepo;
        this.diagnosticTestRepository = diagnosticTestRepository;
    }

    public ActiveIngredientPreRequestedTest create(ActiveIngredientPreRequestedTestCreateVM createVM) {
        LOG.debug("create active ingredient pre-requested test {}", createVM);
        ActiveIngredientPreRequestedTest entity = toEntityForCreate(createVM);
        ActiveIngredientPreRequestedTest saved = preRequestedTestRepository.save(entity);
        LOG.debug("create: saved id={}", saved.getId());
        return saved;
    }

    public ActiveIngredientPreRequestedTest update(ActiveIngredientPreRequestedTestUpdateVM updateVM) {
        LOG.debug("update active ingredient pre-requested test : {} ", updateVM);
        ActiveIngredientPreRequestedTest entity = preRequestedTestRepository.findById(updateVM.id())
                .orElseThrow(() -> new NotFoundAlertException(
                        "ActiveIngredientPreRequestedTest not found: " + updateVM.id(),
                        ENTITY_NAME,
                        "notfound"
                ));
        applyUpdate(entity, updateVM);
        ActiveIngredientPreRequestedTest saved = preRequestedTestRepository.save(entity);
        LOG.debug("update: saved id={}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ActiveIngredientPreRequestedTest> getByActiveIngredientId(Long activeIngredientId) {
        LOG.debug("get active ingredient pre-requested tests by activeIngredientId : q='{}'", activeIngredientId);
        return preRequestedTestRepository.findByActiveIngredientId(activeIngredientId);
    }

    @Transactional
    public void hardDelete(Long id) {
        LOG.debug("delete active ingredient pre-requested test by id : '{}'", id);
        preRequestedTestRepository.deleteById(id);
    }

    // Helpers

    private ActiveIngredientPreRequestedTest toEntityForCreate(ActiveIngredientPreRequestedTestCreateVM vm) {
        ActiveIngredients activeIngredients = getActiveIngredient(vm.activeIngredientId());
        DiagnosticTest diagnosticTest = getDiagnosticTest(vm.testId());
        return ActiveIngredientPreRequestedTest.builder()
                .activeIngredient(activeIngredients)
                .test(diagnosticTest)
                .build();
    }

    private void applyUpdate(ActiveIngredientPreRequestedTest entity, ActiveIngredientPreRequestedTestUpdateVM vm) {
        if (vm.activeIngredientId() != null) {
            entity.setActiveIngredient(getActiveIngredient(vm.activeIngredientId()));
        }
        if (vm.testId() != null) {
            entity.setTest(getDiagnosticTest(vm.testId()));
        }
    }

    private ActiveIngredients getActiveIngredient(Long id) {
        LOG.debug("getActiveIngredient for active ingredients: id={}", id);
        return activeIngredientsRepo.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Active ingredient not found: " + id, "ActiveIngredients", "notfound"));
    }

    private DiagnosticTest getDiagnosticTest(Long id) {
        LOG.debug("getDiagnosticTest for diagnostic test: id={}", id);
        return diagnosticTestRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Diagnostic test not found: " + id, "DiagnosticTest", "notfound"));
    }
}
