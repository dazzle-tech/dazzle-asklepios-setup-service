package com.dazzle.asklepios.web.rest.vm.activeIngredientIndications;

import com.dazzle.asklepios.domain.ActiveIngredientIndications;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record ActiveIngredientIndicationsUpdateVM(
        @NotNull Long id,
        @NotNull Long activeIngredientId,
        @NotNull Long icd10CodeId,
        BigDecimal dosage,
        String unit,
        Boolean isOffLabel

) implements Serializable {

    public static ActiveIngredientIndicationsUpdateVM ofEntity(ActiveIngredientIndications entity) {
        return new ActiveIngredientIndicationsUpdateVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getIcd10Code() != null ? entity.getIcd10Code().getId() : null,
                entity.getDosage(),
                entity.getUnit(),
                entity.getIsOffLabel()
        );
    }
}
