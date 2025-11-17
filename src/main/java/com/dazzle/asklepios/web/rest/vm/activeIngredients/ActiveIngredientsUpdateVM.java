package com.dazzle.asklepios.web.rest.vm.activeIngredients;

import com.dazzle.asklepios.domain.ActiveIngredients;
import com.dazzle.asklepios.domain.enumeration.ActiveIngredientsControlled;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record ActiveIngredientsUpdateVM(
        @NotNull Long id,

        @NotEmpty String name,
        Long drugClassId,

        String atcCode,
        Boolean otc,
        Boolean hasSynonyms,
        Boolean antimicrobial,
        Boolean highRiskMed,
        Boolean abortiveMedication,
        Boolean laborInducingMed,
        Boolean isControlled,
        ActiveIngredientsControlled controlled,
        Boolean hasBlackBoxWarning,
        String blackBoxWarning,
        Boolean isActive,

        String toxicityMaximumDose,
        String toxicityMaximumDosePerUnit,
        String toxicityDetails,
        String mechanismOfAction,
        String pharmaAbsorption,
        String pharmaRouteOfElimination,
        String pharmaVolumeOfDistribution,
        String pharmaHalfLife,
        String pharmaProteinBinding,
        String pharmaClearance,
        String pharmaMetabolism,

        String pregnancyCategory,
        String pregnancyNotes,
        String lactationRisk,
        String lactationRiskNotes,

        Boolean doseAdjustmentRenal,
        String doseAdjustmentRenalOne,
        String doseAdjustmentRenalTwo,
        String doseAdjustmentRenalThree,
        String doseAdjustmentRenalFour,

        Boolean doseAdjustmentHepatic,
        String doseAdjustmentPugA,
        String doseAdjustmentPugB,
        String doseAdjustmentPugC
) implements Serializable {

    public static ActiveIngredientsUpdateVM ofEntity(ActiveIngredients entity) {
        return new ActiveIngredientsUpdateVM(
                entity.getId(),
                entity.getName(),
                entity.getDrugClass() != null ? entity.getDrugClass().getId() : null,
                entity.getAtcCode(),
                entity.getOtc(),
                entity.getHasSynonyms(),
                entity.getAntimicrobial(),
                entity.getHighRiskMed(),
                entity.getAbortiveMedication(),
                entity.getLaborInducingMed(),
                entity.getIsControlled(),
                entity.getControlled(),
                entity.getHasBlackBoxWarning(),
                entity.getBlackBoxWarning(),
                entity.getIsActive(),
                entity.getToxicityMaximumDose(),
                entity.getToxicityMaximumDosePerUnit(),
                entity.getToxicityDetails(),
                entity.getMechanismOfAction(),
                entity.getPharmaAbsorption(),
                entity.getPharmaRouteOfElimination(),
                entity.getPharmaVolumeOfDistribution(),
                entity.getPharmaHalfLife(),
                entity.getPharmaProteinBinding(),
                entity.getPharmaClearance(),
                entity.getPharmaMetabolism(),
                entity.getPregnancyCategory(),
                entity.getPregnancyNotes(),
                entity.getLactationRisk(),
                entity.getLactationRiskNotes(),
                entity.getDoseAdjustmentRenal(),
                entity.getDoseAdjustmentRenalOne(),
                entity.getDoseAdjustmentRenalTwo(),
                entity.getDoseAdjustmentRenalThree(),
                entity.getDoseAdjustmentRenalFour(),
                entity.getDoseAdjustmentHepatic(),
                entity.getDoseAdjustmentPugA(),
                entity.getDoseAdjustmentPugB(),
                entity.getDoseAdjustmentPugC()
        );
    }
}
