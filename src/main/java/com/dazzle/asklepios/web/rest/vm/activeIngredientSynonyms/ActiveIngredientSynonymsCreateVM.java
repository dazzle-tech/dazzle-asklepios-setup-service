package com.dazzle.asklepios.web.rest.vm.activeIngredientSynonyms;

import com.dazzle.asklepios.domain.ActiveIngredientSynonyms;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
@JsonIgnoreProperties(ignoreUnknown = true)

public record ActiveIngredientSynonymsCreateVM (
        @NotNull Long activeIngredientId,
        @NotEmpty String synonym
) implements Serializable
{
    public static ActiveIngredientSynonymsCreateVM ofEntity(ActiveIngredientSynonyms entity) {
        return new ActiveIngredientSynonymsCreateVM(
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getSynonym()
        );}
}
