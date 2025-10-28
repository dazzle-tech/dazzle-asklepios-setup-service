package com.dazzle.asklepios.web.rest.vm.ageGroup;

import com.dazzle.asklepios.domain.AgeGroup;
import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import com.dazzle.asklepios.domain.enumeration.AgeUnit;

import java.io.Serializable;
import java.math.BigDecimal;

public record AgeGroupResponseVM(
        Long id,
        AgeGroupType ageGroup,
        BigDecimal fromAge,
        BigDecimal toAge,
        AgeUnit fromAgeUnit,
        AgeUnit toAgeUnit,
        Long facilityId
) implements Serializable {

    public static AgeGroupResponseVM ofEntity(AgeGroup entity) {
        return new AgeGroupResponseVM(
                entity.getId(),
                entity.getAgeGroup(),
                entity.getFromAge(),
                entity.getToAge(),
                entity.getFromAgeUnit(),
                entity.getToAgeUnit(),
                entity.getFacility() != null ? entity.getFacility().getId() : null
        );
    }
}
