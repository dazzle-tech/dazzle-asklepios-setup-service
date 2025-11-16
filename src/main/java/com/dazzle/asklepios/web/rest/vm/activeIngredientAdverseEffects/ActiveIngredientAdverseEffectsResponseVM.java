package com.dazzle.asklepios.web.rest.vm.activeIngredientAdverseEffects;

import com.dazzle.asklepios.domain.ActiveIngredientAdverseEffects;

import java.io.Serializable;

public record ActiveIngredientAdverseEffectsResponseVM(
        Long id,
        Long activeIngredientId,
        String adverseEffect
) implements Serializable {
    public static ActiveIngredientAdverseEffectsResponseVM ofEntity(ActiveIngredientAdverseEffects entity) {
        return new ActiveIngredientAdverseEffectsResponseVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getAdverseEffect()
        );
    }
}