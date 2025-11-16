package com.dazzle.asklepios.web.rest.vm.activeIngredientPreRequestedTest;


import com.dazzle.asklepios.domain.ActiveIngredientPreRequestedTest;

import java.io.Serializable;

public record ActiveIngredientPreRequestedTestResponseVM(
        Long Id,
        Long activeIngredientId,
        Long testId
) implements Serializable {
    public static ActiveIngredientPreRequestedTestResponseVM ofEntity(ActiveIngredientPreRequestedTest entity) {
        return new ActiveIngredientPreRequestedTestResponseVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getTest()!=null? entity.getTest().getId() : null
        );
    }
}
