package com.dazzle.asklepios.web.rest.vm.brandMedication.search;


import com.dazzle.asklepios.domain.ActiveIngredients;

import java.math.BigDecimal;

public record ActiveIngredientInBrandVM(
        Long id,
        String name,
        String atcCode,
        BigDecimal strength,
        String unit
) {
    public static ActiveIngredientInBrandVM of(
            ActiveIngredients ai,
            BigDecimal strength,
            String unit
    ) {
        return new ActiveIngredientInBrandVM(
                ai.getId(),
                ai.getName(),
                ai.getAtcCode(),
                strength,
                unit
        );
    }
}
