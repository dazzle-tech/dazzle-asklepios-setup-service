package com.dazzle.asklepios.web.rest.vm.activeIngredientFoodInteractions;

import com.dazzle.asklepios.domain.ActiveIngredientFoodInteractions;

import java.io.Serializable;

public record ActiveIngredientFoodInteractionsResponseVM(
        Long Id,
        Long activeIngredientId,
        String food,
        String severity,
        String description
) implements Serializable {
    public static ActiveIngredientFoodInteractionsResponseVM ofEntity(ActiveIngredientFoodInteractions entity) {
        return new ActiveIngredientFoodInteractionsResponseVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getFood(),
                entity.getSeverity(),
                entity.getDescription()
        );
    }
}
