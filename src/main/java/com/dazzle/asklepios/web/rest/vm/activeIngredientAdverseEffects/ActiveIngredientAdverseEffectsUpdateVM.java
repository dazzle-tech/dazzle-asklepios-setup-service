package com.dazzle.asklepios.web.rest.vm.activeIngredientAdverseEffects;

import com.dazzle.asklepios.domain.ActiveIngredientAdverseEffects;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record ActiveIngredientAdverseEffectsUpdateVM(
        @NotNull Long id,
        @NotNull Long activeIngredientId,
        @NotEmpty String adverseEffect
) implements Serializable {

    public static ActiveIngredientAdverseEffectsUpdateVM ofEntity(ActiveIngredientAdverseEffects entity) {
        return new ActiveIngredientAdverseEffectsUpdateVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
               entity.getAdverseEffect()
        );
    }
}
