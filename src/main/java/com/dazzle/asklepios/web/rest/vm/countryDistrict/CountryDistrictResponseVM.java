package com.dazzle.asklepios.web.rest.vm.countryDistrict;

import com.dazzle.asklepios.domain.CountryDistrict;

import java.io.Serializable;

public record CountryDistrictResponseVM(
        Long id,
        Long countryId,
        String name,
        String code,
        Boolean isActive
) implements Serializable {

    public static CountryDistrictResponseVM ofEntity(CountryDistrict entity) {
        return new CountryDistrictResponseVM(
                entity.getId(),
                entity.getCountry() != null ? entity.getCountry().getId() : null,
                entity.getName(),
                entity.getCode(),
                entity.getIsActive()
        );
    }
}
