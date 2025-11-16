package com.dazzle.asklepios.web.rest.vm.activeIngredientDrugInteractions;

import com.dazzle.asklepios.domain.ActiveIngredientDrugInteractions;

import java.io.Serializable;

public record ActiveIngredientDrugInteractionsResponseVM(
        Long Id,
        Long activeIngredientId,
        Long interactedIngredientId,
        String severity,
        String description
) implements Serializable {
    public static ActiveIngredientDrugInteractionsResponseVM ofEntity(ActiveIngredientDrugInteractions entity) {
        return new ActiveIngredientDrugInteractionsResponseVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getInteractedActiveIngredient()!=null ? entity.getInteractedActiveIngredient().getId() : null,
                entity.getSeverity(),
                entity.getDescription()
        );
    }
}
