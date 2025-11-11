package com.dazzle.asklepios.web.rest.vm.allergens;

import com.dazzle.asklepios.domain.Allergens;
import com.dazzle.asklepios.domain.enumeration.AllergenType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * View Model for updating an Allergen via REST.
 */
public record AllergensUpdateVM(
        @NotNull Long id,
        @NotEmpty String name,
        @NotNull AllergenType type,
        String description,
        @NotNull Boolean isActive
) implements Serializable {

    public static AllergensUpdateVM ofEntity(Allergens allergen) {
        return new AllergensUpdateVM(
                allergen.getId(),
                allergen.getName(),
                allergen.getType(),
                allergen.getDescription(),
                allergen.getIsActive()
        );
    }
}
