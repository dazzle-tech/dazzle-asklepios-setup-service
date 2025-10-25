package com.dazzle.asklepios.web.rest.vm.allergens;

import com.dazzle.asklepios.domain.Allergens;
import com.dazzle.asklepios.domain.enumeration.AllergenType;

import java.io.Serializable;

public record AllergensResponseVM(
        Long id,
        String code,
        String name,
        AllergenType type,
        String description,
        Boolean isActive
) implements Serializable {

    public static AllergensResponseVM ofEntity(Allergens allergen) {
        return new AllergensResponseVM(
                allergen.getId(),
                allergen.getCode(),
                allergen.getName(),
                allergen.getType(),
                allergen.getDescription(),
                allergen.getIsActive()
        );
    }
}
