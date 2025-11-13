package com.dazzle.asklepios.web.rest.vm.activeIngredientSpecialPopulations;

import com.dazzle.asklepios.domain.ActiveIngredientSpecialPopulations;

import java.io.Serializable;

public record ActiveIngredientSpecialPopulationsResponseVM(
        Long Id,
        Long activeIngredientId,
        String additionalPopulation,
        String considerations
) implements Serializable {
    public static ActiveIngredientSpecialPopulationsResponseVM ofEntity(ActiveIngredientSpecialPopulations entity) {
        return new ActiveIngredientSpecialPopulationsResponseVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getAdditionalPopulation(),
                entity.getConsiderations()
        );
    }

}
