package com.dazzle.asklepios.web.rest.vm.activeIngredientDrugInteractions;

import com.dazzle.asklepios.domain.ActiveIngredientDrugInteractions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActiveIngredientDrugInteractionsCreateVM(
        @NotNull Long activeIngredientId,
        @NotNull Long interactedIngredientId,
        @NotNull String severity,
        @NotEmpty String description
) implements Serializable {
    public static ActiveIngredientDrugInteractionsCreateVM ofEntity(ActiveIngredientDrugInteractions entity) {
        return new ActiveIngredientDrugInteractionsCreateVM(
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getInteractedActiveIngredient() != null ? entity.getInteractedActiveIngredient().getId() : null,
                entity.getSeverity(),
                entity.getDescription()
        );
    }
}
