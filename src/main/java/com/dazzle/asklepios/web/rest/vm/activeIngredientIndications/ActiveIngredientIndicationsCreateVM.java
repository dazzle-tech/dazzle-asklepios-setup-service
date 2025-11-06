package com.dazzle.asklepios.web.rest.vm.activeIngredientIndications;

import com.dazzle.asklepios.domain.ActiveIngredientIndications;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActiveIngredientIndicationsCreateVM(
        @NotNull Long activeIngredientId,
        @NotNull Long icd10CodeId,
        BigDecimal dosage,
        String unit,
        Boolean isOffLabel

) implements Serializable {

    public static ActiveIngredientIndicationsCreateVM ofEntity(ActiveIngredientIndications entity) {
        return new ActiveIngredientIndicationsCreateVM(
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getIcd10Code() != null ? entity.getIcd10Code().getId() : null,
                entity.getDosage(),
                entity.getUnit(),
                entity.getIsOffLabel()
        );
    }
}
