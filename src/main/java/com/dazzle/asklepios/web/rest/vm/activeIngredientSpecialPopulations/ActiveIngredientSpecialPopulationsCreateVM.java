package com.dazzle.asklepios.web.rest.vm.activeIngredientSpecialPopulations;

import com.dazzle.asklepios.domain.ActiveIngredientSpecialPopulations;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActiveIngredientSpecialPopulationsCreateVM(
        @NotNull Long activeIngredientId,
        String additionalPopulation,
        @NotEmpty String considerations
) implements Serializable {
    public static ActiveIngredientSpecialPopulationsCreateVM ofEntity(ActiveIngredientSpecialPopulations entity) {
        return new ActiveIngredientSpecialPopulationsCreateVM(
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getAdditionalPopulation(),
                entity.getConsiderations()
        );
    }
}
