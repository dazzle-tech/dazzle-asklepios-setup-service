package com.dazzle.asklepios.web.rest.vm.activeIngredientContraindications;

import com.dazzle.asklepios.domain.ActiveIngredientContraindications;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record ActiveIngredientContraindicationsUpdateVM(
        @NotNull Long id,
        @NotNull Long activeIngredientId,
        @NotNull Long icd10CodeId
) implements Serializable {

    public static ActiveIngredientContraindicationsUpdateVM ofEntity(ActiveIngredientContraindications entity) {
        return new ActiveIngredientContraindicationsUpdateVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getIcd10Code() != null ? entity.getIcd10Code().getId() : null
        );
    }
}
