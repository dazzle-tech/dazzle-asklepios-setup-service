package com.dazzle.asklepios.web.rest.vm.activeIngredientSynonyms;

import com.dazzle.asklepios.domain.ActiveIngredientSynonyms;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record ActiveIngredientSynonymsUpdateVM(
        @NotNull Long id,
        @NotNull Long activeIngredientId,
        @NotEmpty String synonym
)
    implements Serializable
    {
        public static ActiveIngredientSynonymsUpdateVM ofEntity(ActiveIngredientSynonyms entity) {
        return new ActiveIngredientSynonymsUpdateVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getSynonym()
        );}
    }

