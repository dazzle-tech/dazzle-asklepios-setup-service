package com.dazzle.asklepios.web.rest.vm.ageGroup;

import com.dazzle.asklepios.domain.AgeGroup;
import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import com.dazzle.asklepios.domain.enumeration.AgeUnit;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgeGroupCreateVM(
        @NotNull AgeGroupType ageGroup,
        @NotNull @PositiveOrZero BigDecimal fromAge,
        @NotNull @PositiveOrZero BigDecimal toAge,
        @NotNull AgeUnit fromAgeUnit,
        @NotNull AgeUnit toAgeUnit
) implements Serializable {

    public static AgeGroupCreateVM ofEntity(AgeGroup entity) {
        return new AgeGroupCreateVM(
                entity.getAgeGroup(),
                entity.getFromAge(),
                entity.getToAge(),
                entity.getFromAgeUnit(),
                entity.getToAgeUnit()
        );
    }
}
