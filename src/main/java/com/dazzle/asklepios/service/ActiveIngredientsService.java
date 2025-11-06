package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ActiveIngredients;
import com.dazzle.asklepios.domain.MedicationCategories;
import com.dazzle.asklepios.domain.MedicationCategoriesClass;
import com.dazzle.asklepios.repository.ActiveIngredientsRepository;
import com.dazzle.asklepios.repository.MedicationCategoriesClassRepository;
import com.dazzle.asklepios.repository.MedicationCategoriesRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.activeIngredients.ActiveIngredientsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredients.ActiveIngredientsUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class ActiveIngredientsService {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientsService.class);

    private final ActiveIngredientsRepository activeRepo;
    private final MedicationCategoriesRepository categoryRepo;
    private final MedicationCategoriesClassRepository classRepo;

    private static final String ENTITY_NAME = "ActiveIngredients";


    public ActiveIngredientsService(
            ActiveIngredientsRepository activeRepo,
            MedicationCategoriesRepository categoryRepo,
            MedicationCategoriesClassRepository classRepo
    ) {
        this.activeRepo = activeRepo;
        this.categoryRepo = categoryRepo;
        this.classRepo = classRepo;
    }

    // 1) create
    public ActiveIngredients create(ActiveIngredientsCreateVM activeIngredientsCreateVM) {
        LOG.debug("create active ingredient {}", activeIngredientsCreateVM);
        ActiveIngredients entity = toEntityForCreate(activeIngredientsCreateVM);
        try {
            ActiveIngredients activeIngredients = activeRepo.saveAndFlush(entity);
            LOG.debug("create: saved id={}", activeIngredients.getId());
            return activeIngredients;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage()).toLowerCase();
            LOG.error("Database constraint violation while creating ActiveIngredients: {}", message, ex);
            if (message.contains("uq_ai_name") ||
                    message.contains("unique constraint") ||
                    message.contains("duplicate key") ||
                    message.contains("duplicate entry")) {
                throw new BadRequestAlertException(
                        "An active ingredient with the same name  already exists.",
                        ENTITY_NAME,
                        "unique.name.activeIngredients"
                );
            }
            throw new BadRequestAlertException(
                    "Database constraint violated while creating active ingredient (check unique name, or required fields).",
                    ENTITY_NAME,
                    "db.constraint"
            );
        }
    }

    // 2) update (partial, applies only non-null fields)
    public ActiveIngredients update(ActiveIngredientsUpdateVM activeIngredientsUpdateVM) {
        LOG.debug("update active ingredients: {} ", activeIngredientsUpdateVM);
        ActiveIngredients entity = activeRepo.findById(activeIngredientsUpdateVM.id())
                .orElseThrow(() -> new NotFoundAlertException("ActiveIngredients not found: " + activeIngredientsUpdateVM.id(), ENTITY_NAME, "notfound"));
        applyUpdate(entity, activeIngredientsUpdateVM);
       try{
           ActiveIngredients saved = activeRepo.save(entity);
        LOG.debug("update: saved id={}", saved.getId());
        return saved;
       } catch (DataIntegrityViolationException | JpaSystemException ex) {
        Throwable root = getRootCause(ex);
        String message = (root != null ? root.getMessage() : ex.getMessage()).toLowerCase();
        LOG.error("Database constraint violation while updating ActiveIngredients: {}", message, ex);
        if (message.contains("uq_ai_name") ||
                message.contains("unique constraint") ||
                message.contains("duplicate key") ||
                message.contains("duplicate entry")) {
            throw new BadRequestAlertException(
                    "An active ingredient with the same name  already exists.",
                    ENTITY_NAME,
                    "unique.name.activeIngredients"
            );
        }
        throw new BadRequestAlertException(
                "Database constraint violated while updating active ingredient (check unique name, or required fields).",
                ENTITY_NAME,
                "db.constraint"
        );
    }
    }

    // 3) get all (pageable)
    @Transactional(readOnly = true)
    public Page<ActiveIngredients> getAll(Pageable pageable) {
        LOG.debug("get all active ingredients: page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return activeRepo.findAll(pageable);
    }

    // 4) get by name (pageable, contains+ignoreCase)
    @Transactional(readOnly = true)
    public Page<ActiveIngredients> getByName(String name, Pageable pageable) {
        LOG.debug("get active ingredients by name : q='{}' page={} size={}", name, pageable.getPageNumber(), pageable.getPageSize());
        return activeRepo.findByNameContainsIgnoreCase(name, pageable);
    }



    // 6) get by drug class id (pageable)
    @Transactional(readOnly = true)
    public Page<ActiveIngredients> getByDrugClassId(Long drugClassId, Pageable pageable) {
        LOG.debug("get active ingredients by drugClassId: drugClassId={} page={} size={}", drugClassId, pageable.getPageNumber(), pageable.getPageSize());
        return activeRepo.findByDrugClassId(drugClassId, pageable);
    }

    // 7) get by atc code (pageable, contains+ignoreCase)
    @Transactional(readOnly = true)
    public Page<ActiveIngredients> getByAtcCode(String atcCode, Pageable pageable) {
        LOG.debug("get active ingredients by atcCode: q='{}' page={} size={}", atcCode, pageable.getPageNumber(), pageable.getPageSize());
        return activeRepo.findByAtcCodeContainsIgnoreCase(atcCode, pageable);
    }

    // 8) toggle active
    public ActiveIngredients toggleActive(Long id) {
        LOG.debug("toggleActive for active ingredient: id={}", id);
        ActiveIngredients entity = activeRepo.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("ActiveIngredients not found: " + id, ENTITY_NAME, "notfound"));
        entity.setIsActive(Boolean.FALSE.equals(entity.getIsActive()));
        ActiveIngredients saved = activeRepo.save(entity);
        LOG.debug("toggleActive for active ingredient: id={} newIsActive={}", id, saved.getIsActive());
        return saved;
    }

    // Helpers

    private ActiveIngredients toEntityForCreate(ActiveIngredientsCreateVM vm) {
        MedicationCategoriesClass drugClass = resolveClass(vm.drugClassId());

        ActiveIngredients ai = new ActiveIngredients();
        ai.setName(vm.name());
        ai.setDrugClass(drugClass);
        ai.setAtcCode(vm.atcCode());
        ai.setOtc(vm.otc());
        ai.setHasSynonyms(vm.hasSynonyms());
        ai.setAntimicrobial(vm.antimicrobial());
        ai.setHighRiskMed(vm.highRiskMed());
        ai.setAbortiveMedication(vm.abortiveMedication());
        ai.setLaborInducingMed(vm.laborInducingMed());
        ai.setIsControlled(vm.isControlled());
        ai.setControlled(vm.controlled());
        ai.setHasBlackBoxWarning(vm.hasBlackBoxWarning());
        ai.setBlackBoxWarning(vm.blackBoxWarning());
        ai.setIsActive(vm.isActive());

        ai.setToxicityMaximumDose(vm.toxicityMaximumDose());
        ai.setToxicityMaximumDosePerUnit(vm.toxicityMaximumDosePerUnit());
        ai.setToxicityDetails(vm.toxicityDetails());
        ai.setMechanismOfAction(vm.mechanismOfAction());
        ai.setPharmaAbsorption(vm.pharmaAbsorption());
        ai.setPharmaRouteOfElimination(vm.pharmaRouteOfElimination());
        ai.setPharmaVolumeOfDistribution(vm.pharmaVolumeOfDistribution());
        ai.setPharmaHalfLife(vm.pharmaHalfLife());
        ai.setPharmaProteinBinding(vm.pharmaProteinBinding());
        ai.setPharmaClearance(vm.pharmaClearance());
        ai.setPharmaMetabolism(vm.pharmaMetabolism());
        ai.setPregnancyCategory(vm.pregnancyCategory());
        ai.setPregnancyNotes(vm.pregnancyNotes());
        ai.setLactationRisk(vm.lactationRisk());
        ai.setLactationRiskNotes(vm.lactationRiskNotes());
        ai.setDoseAdjustmentRenal(vm.doseAdjustmentRenal());
        ai.setDoseAdjustmentRenalOne(vm.doseAdjustmentRenalOne());
        ai.setDoseAdjustmentRenalTwo(vm.doseAdjustmentRenalTwo());
        ai.setDoseAdjustmentRenalThree(vm.doseAdjustmentRenalThree());
        ai.setDoseAdjustmentRenalFour(vm.doseAdjustmentRenalFour());
        ai.setDoseAdjustmentHepatic(vm.doseAdjustmentHepatic());
        ai.setDoseAdjustmentPugA(vm.doseAdjustmentPugA());
        ai.setDoseAdjustmentPugB(vm.doseAdjustmentPugB());
        ai.setDoseAdjustmentPugC(vm.doseAdjustmentPugC());
        return ai;
    }

    private void applyUpdate(ActiveIngredients ai, ActiveIngredientsUpdateVM vm) {
        if (vm.name() != null) ai.setName(vm.name());
        if (vm.drugClassId() != null) ai.setDrugClass(resolveClass(vm.drugClassId()));
        if (vm.atcCode() != null) ai.setAtcCode(vm.atcCode());
        if (vm.otc() != null) ai.setOtc(vm.otc());
        if (vm.hasSynonyms() != null) ai.setHasSynonyms(vm.hasSynonyms());
        if (vm.antimicrobial() != null) ai.setAntimicrobial(vm.antimicrobial());
        if (vm.highRiskMed() != null) ai.setHighRiskMed(vm.highRiskMed());
        if (vm.abortiveMedication() != null) ai.setAbortiveMedication(vm.abortiveMedication());
        if (vm.laborInducingMed() != null) ai.setLaborInducingMed(vm.laborInducingMed());
        if (vm.isControlled() != null) ai.setIsControlled(vm.isControlled());
        if (vm.controlled() != null) ai.setControlled(vm.controlled());
        if (vm.hasBlackBoxWarning() != null) ai.setHasBlackBoxWarning(vm.hasBlackBoxWarning());
        if (vm.blackBoxWarning() != null) ai.setBlackBoxWarning(vm.blackBoxWarning());
        if (vm.isActive() != null) ai.setIsActive(vm.isActive());

        if (vm.toxicityMaximumDose() != null) ai.setToxicityMaximumDose(vm.toxicityMaximumDose());
        if (vm.toxicityMaximumDosePerUnit() != null) ai.setToxicityMaximumDosePerUnit(vm.toxicityMaximumDosePerUnit());
        if (vm.toxicityDetails() != null) ai.setToxicityDetails(vm.toxicityDetails());
        if (vm.mechanismOfAction() != null) ai.setMechanismOfAction(vm.mechanismOfAction());
        if (vm.pharmaAbsorption() != null) ai.setPharmaAbsorption(vm.pharmaAbsorption());
        if (vm.pharmaRouteOfElimination() != null) ai.setPharmaRouteOfElimination(vm.pharmaRouteOfElimination());
        if (vm.pharmaVolumeOfDistribution() != null) ai.setPharmaVolumeOfDistribution(vm.pharmaVolumeOfDistribution());
        if (vm.pharmaHalfLife() != null) ai.setPharmaHalfLife(vm.pharmaHalfLife());
        if (vm.pharmaProteinBinding() != null) ai.setPharmaProteinBinding(vm.pharmaProteinBinding());
        if (vm.pharmaClearance() != null) ai.setPharmaClearance(vm.pharmaClearance());
        if (vm.pharmaMetabolism() != null) ai.setPharmaMetabolism(vm.pharmaMetabolism());
        if (vm.pregnancyCategory() != null) ai.setPregnancyCategory(vm.pregnancyCategory());
        if (vm.pregnancyNotes() != null) ai.setPregnancyNotes(vm.pregnancyNotes());
        if (vm.lactationRisk() != null) ai.setLactationRisk(vm.lactationRisk());
        if (vm.lactationRiskNotes() != null) ai.setLactationRiskNotes(vm.lactationRiskNotes());
        if (vm.doseAdjustmentRenal() != null) ai.setDoseAdjustmentRenal(vm.doseAdjustmentRenal());
        if (vm.doseAdjustmentRenalOne() != null) ai.setDoseAdjustmentRenalOne(vm.doseAdjustmentRenalOne());
        if (vm.doseAdjustmentRenalTwo() != null) ai.setDoseAdjustmentRenalTwo(vm.doseAdjustmentRenalTwo());
        if (vm.doseAdjustmentRenalThree() != null) ai.setDoseAdjustmentRenalThree(vm.doseAdjustmentRenalThree());
        if (vm.doseAdjustmentRenalFour() != null) ai.setDoseAdjustmentRenalFour(vm.doseAdjustmentRenalFour());
        if (vm.doseAdjustmentHepatic() != null) ai.setDoseAdjustmentHepatic(vm.doseAdjustmentHepatic());
        if (vm.doseAdjustmentPugA() != null) ai.setDoseAdjustmentPugA(vm.doseAdjustmentPugA());
        if (vm.doseAdjustmentPugB() != null) ai.setDoseAdjustmentPugB(vm.doseAdjustmentPugB());
        if (vm.doseAdjustmentPugC() != null) ai.setDoseAdjustmentPugC(vm.doseAdjustmentPugC());
    }


    private MedicationCategoriesClass resolveClass(Long id) {
        LOG.debug("resolveClass for active ingredients: id={}", id);
        return classRepo.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Medical category class not found: " + id, "MedicalCategoriesClass", "notfound"));
    }
}
