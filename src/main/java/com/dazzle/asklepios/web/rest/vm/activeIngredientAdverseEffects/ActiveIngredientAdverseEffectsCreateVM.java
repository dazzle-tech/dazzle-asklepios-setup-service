package com.dazzle.asklepios.web.rest.vm.activeIngredientAdverseEffects;

import com.dazzle.asklepios.domain.ActiveIngredientAdverseEffects;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActiveIngredientAdverseEffectsCreateVM (
        @NotNull Long activeIngredientId,
        @NotEmpty String adverseEffect
) implements Serializable {

    public static ActiveIngredientAdverseEffectsCreateVM ofEntity(ActiveIngredientAdverseEffects entity) {
        return new ActiveIngredientAdverseEffectsCreateVM(
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                 entity.getAdverseEffect()
        );
    }
}
