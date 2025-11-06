package com.dazzle.asklepios.web.rest.vm.activeIngredientIndications;

import com.dazzle.asklepios.domain.ActiveIngredientIndications;

import java.io.Serializable;
import java.math.BigDecimal;

public record ActiveIngredientIndicationsResponseVM(
        Long id,
        Long activeIngredientId,
        Long icd10CodeId,
        BigDecimal dosage,
        String unit,
        Boolean isOffLabel
) implements Serializable {
    public static ActiveIngredientIndicationsResponseVM ofEntity(ActiveIngredientIndications entity) {
        return new ActiveIngredientIndicationsResponseVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getIcd10Code() != null ? entity.getIcd10Code().getId() : null,
                entity.getDosage(),
                entity.getUnit(),
                entity.getIsOffLabel()
        );
    }
}
