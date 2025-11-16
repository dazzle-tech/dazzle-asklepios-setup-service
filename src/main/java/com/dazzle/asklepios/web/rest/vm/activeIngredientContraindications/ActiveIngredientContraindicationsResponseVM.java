package com.dazzle.asklepios.web.rest.vm.activeIngredientContraindications;

import com.dazzle.asklepios.domain.ActiveIngredientContraindications;

import java.io.Serializable;

public record ActiveIngredientContraindicationsResponseVM (
        Long id,
        Long activeIngredientId,
        Long icd10CodeId
) implements Serializable {
    public static ActiveIngredientContraindicationsResponseVM ofEntity(ActiveIngredientContraindications entity) {
        return new ActiveIngredientContraindicationsResponseVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getIcd10Code() != null ? entity.getIcd10Code().getId() : null
        );
    }
}
