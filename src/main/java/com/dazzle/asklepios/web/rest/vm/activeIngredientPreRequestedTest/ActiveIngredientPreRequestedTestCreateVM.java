package com.dazzle.asklepios.web.rest.vm.activeIngredientPreRequestedTest;

import com.dazzle.asklepios.domain.ActiveIngredientPreRequestedTest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActiveIngredientPreRequestedTestCreateVM(
        @NotNull Long activeIngredientId,
        @NotNull Long testId
) implements Serializable {
    public static ActiveIngredientPreRequestedTestCreateVM ofEntity(ActiveIngredientPreRequestedTest entity) {
        return new ActiveIngredientPreRequestedTestCreateVM(
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getTest() != null ? entity.getTest().getId() : null
        );
    }
}
