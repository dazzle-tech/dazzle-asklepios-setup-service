package com.dazzle.asklepios.web.rest.vm.activeIngredientPreRequestedTest;

import com.dazzle.asklepios.domain.ActiveIngredientPreRequestedTest;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record ActiveIngredientPreRequestedTestUpdateVM(
        @NotNull Long id,
        @NotNull Long activeIngredientId,
        @NotNull Long testId
) implements Serializable {
    public static ActiveIngredientPreRequestedTestUpdateVM ofEntity(ActiveIngredientPreRequestedTest entity) {
        return new ActiveIngredientPreRequestedTestUpdateVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getTest() != null ? entity.getTest().getId() : null
        );
    }
}
