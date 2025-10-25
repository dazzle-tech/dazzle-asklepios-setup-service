package com.dazzle.asklepios.web.rest.vm.allergens;

import com.dazzle.asklepios.domain.Allergens;
import com.dazzle.asklepios.domain.enumeration.AllergenType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AllergensCreateVM(
        @NotEmpty String code,
        @NotEmpty String name,
        @NotNull AllergenType type,
        String description,
        Boolean isActive
) implements Serializable {

    public static AllergensCreateVM ofEntity(Allergens allergen) {
        return new AllergensCreateVM(
                allergen.getCode(),
                allergen.getName(),
                allergen.getType(),
                allergen.getDescription(),
                allergen.getIsActive()
        );
    }
}
