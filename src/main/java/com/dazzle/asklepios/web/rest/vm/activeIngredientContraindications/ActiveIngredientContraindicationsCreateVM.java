package com.dazzle.asklepios.web.rest.vm.activeIngredientContraindications;

import com.dazzle.asklepios.domain.ActiveIngredientContraindications;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActiveIngredientContraindicationsCreateVM (
        @NotNull Long activeIngredientId,
        @NotNull Long icd10CodeId
) implements Serializable {

    public static ActiveIngredientContraindicationsCreateVM ofEntity(ActiveIngredientContraindications entity) {
        return new ActiveIngredientContraindicationsCreateVM(
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getIcd10Code() != null ? entity.getIcd10Code().getId() : null
        );
    }
}
