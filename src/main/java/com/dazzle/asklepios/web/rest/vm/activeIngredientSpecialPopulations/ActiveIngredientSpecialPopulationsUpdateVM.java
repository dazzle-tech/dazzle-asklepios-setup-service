package com.dazzle.asklepios.web.rest.vm.activeIngredientSpecialPopulations;

import com.dazzle.asklepios.domain.ActiveIngredientSpecialPopulations;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record ActiveIngredientSpecialPopulationsUpdateVM(
        @NotNull Long id,
        @NotNull Long activeIngredientId,
        String additionalPopulation,
        @NotEmpty String considerations
) implements Serializable {
    public static ActiveIngredientSpecialPopulationsUpdateVM ofEntity(ActiveIngredientSpecialPopulations entity) {
        return new ActiveIngredientSpecialPopulationsUpdateVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getAdditionalPopulation(),
                entity.getConsiderations()
        );
    }

}
