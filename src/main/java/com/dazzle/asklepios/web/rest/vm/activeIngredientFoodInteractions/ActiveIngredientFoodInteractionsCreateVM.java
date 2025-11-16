package com.dazzle.asklepios.web.rest.vm.activeIngredientFoodInteractions;

import com.dazzle.asklepios.domain.ActiveIngredientFoodInteractions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActiveIngredientFoodInteractionsCreateVM(
        @NotNull Long activeIngredientId,
        @NotEmpty String food,
        @NotNull String severity,
        @NotEmpty String description
) implements Serializable {
    public static ActiveIngredientFoodInteractionsCreateVM ofEntity(ActiveIngredientFoodInteractions entity) {
        return new ActiveIngredientFoodInteractionsCreateVM(
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getFood(),
                entity.getSeverity(),
                entity.getDescription()
        );
    }
}
