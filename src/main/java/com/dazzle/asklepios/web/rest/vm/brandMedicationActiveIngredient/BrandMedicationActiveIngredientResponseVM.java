package com.dazzle.asklepios.web.rest.vm.brandMedicationActiveIngredient;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.BrandMedicationActiveIngredient;
import com.dazzle.asklepios.domain.BrandMedicationSubstitute;
import com.dazzle.asklepios.web.rest.vm.activeIngredients.ActiveIngredientsResponseVM;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BrandMedicationActiveIngredientResponseVM(
        Long id,
        Long brandId,
        Long activeIngredientId,
        BigDecimal strength,
        String unit,
        ActiveIngredientsResponseVM activeIngredient


) {
    public static BrandMedicationActiveIngredientResponseVM ofEntity(BrandMedicationActiveIngredient entity) {
        if (entity == null) return null;
        return new BrandMedicationActiveIngredientResponseVM(
                entity.getId(),
                entity.getBrandMedication() != null ? entity.getBrandMedication().getId() : null,
                entity.getActiveIngredients() != null ? entity.getActiveIngredients().getId() : null,
                entity.getStrength(),
                entity.getUnit(),
                ActiveIngredientsResponseVM.ofEntity(entity.getActiveIngredients())
                );
    }
}
