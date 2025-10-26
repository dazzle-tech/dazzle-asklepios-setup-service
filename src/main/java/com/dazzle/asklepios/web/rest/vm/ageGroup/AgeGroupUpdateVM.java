package com.dazzle.asklepios.web.rest.vm.ageGroup;

import com.dazzle.asklepios.domain.AgeGroup;
import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import com.dazzle.asklepios.domain.enumeration.AgeUnit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.math.BigDecimal;

public record AgeGroupUpdateVM(
        @NotNull Long id,
        @NotNull AgeGroupType ageGroup,
        @NotNull @PositiveOrZero BigDecimal fromAge,
        @NotNull @PositiveOrZero BigDecimal toAge,
        @NotNull AgeUnit fromAgeUnit,
        @NotNull AgeUnit toAgeUnit,
        @NotNull Boolean isActive,
        String lastModifiedBy,
        @NotNull Long facilityId
) implements Serializable {

    public static AgeGroupUpdateVM ofEntity(AgeGroup entity) {
        return new AgeGroupUpdateVM(
                entity.getId(),
                entity.getAgeGroup(),
                entity.getFromAge(),
                entity.getToAge(),
                entity.getFromAgeUnit(),
                entity.getToAgeUnit(),
                entity.getIsActive(),
                entity.getLastModifiedBy(),
                entity.getFacility() != null ? entity.getFacility().getId() : null
        );
    }
}
