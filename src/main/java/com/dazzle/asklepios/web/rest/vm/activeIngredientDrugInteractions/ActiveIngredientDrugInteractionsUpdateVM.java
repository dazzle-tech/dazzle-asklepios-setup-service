package com.dazzle.asklepios.web.rest.vm.activeIngredientDrugInteractions;

import com.dazzle.asklepios.domain.ActiveIngredientDrugInteractions;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record ActiveIngredientDrugInteractionsUpdateVM(
        @NotNull Long id,
        @NotNull Long activeIngredientId,
        @NotNull Long interactedIngredientId,
        @NotNull String severity,
        @NotEmpty String description
) implements Serializable {
    public static ActiveIngredientDrugInteractionsUpdateVM ofEntity(ActiveIngredientDrugInteractions entity) {
        return new ActiveIngredientDrugInteractionsUpdateVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getInteractedActiveIngredient()!=null ? entity.getInteractedActiveIngredient().getId() : null,
                entity.getSeverity(),
                entity.getDescription()
        );
    }
}