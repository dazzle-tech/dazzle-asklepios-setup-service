package com.dazzle.asklepios.web.rest.vm.activeIngredientFoodInteractions;

import com.dazzle.asklepios.domain.ActiveIngredientFoodInteractions;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record ActiveIngredientFoodInteractionsUpdateVM(
        @NotNull Long id,
        @NotNull Long activeIngredientId,
        @NotEmpty String food,
        @NotNull String severity,
        @NotEmpty String description
) implements Serializable {
    public static ActiveIngredientFoodInteractionsUpdateVM ofEntity(ActiveIngredientFoodInteractions entity) {
        return new ActiveIngredientFoodInteractionsUpdateVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getFood(),
                entity.getSeverity(),
                entity.getDescription()
        );
    }
}