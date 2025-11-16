package com.dazzle.asklepios.web.rest.vm.activeIngredientSynonyms;

import com.dazzle.asklepios.domain.ActiveIngredientSynonyms;

import java.io.Serializable;

public record ActiveIngredientSynonymsResponseVM(
        Long Id,
        Long activeIngredientId,
        String synonym
) implements Serializable {
    public static ActiveIngredientSynonymsResponseVM ofEntity(ActiveIngredientSynonyms entity) {
        return new ActiveIngredientSynonymsResponseVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getSynonym()
        );
    }
}