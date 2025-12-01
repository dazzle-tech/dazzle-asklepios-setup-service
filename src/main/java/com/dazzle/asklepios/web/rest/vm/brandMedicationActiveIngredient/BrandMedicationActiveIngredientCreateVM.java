package com.dazzle.asklepios.web.rest.vm.brandMedicationActiveIngredient;

import com.dazzle.asklepios.domain.BrandMedicationActiveIngredient;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record
BrandMedicationActiveIngredientCreateVM(
        @NotNull Long brandId,
        @NotNull Long activeIngredientId,
        BigDecimal strength,
        String unit
) {
    public static BrandMedicationActiveIngredientCreateVM ofEntity(BrandMedicationActiveIngredient entity) {
        if (entity == null) return null;
        return new BrandMedicationActiveIngredientCreateVM(
                entity.getBrandMedication() != null ? entity.getBrandMedication().getId() : null,
                entity.getActiveIngredients() != null ? entity.getActiveIngredients().getId() : null,
                entity.getStrength(),
                entity.getUnit()
        );
    }
}
