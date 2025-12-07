package com.dazzle.asklepios.web.rest.vm.countryDistrict;

import com.dazzle.asklepios.domain.CountryDistrict;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record CountryDistrictUpdateVM(
        @NotNull Long id,
        @NotNull Long countryId,
        @NotEmpty String name,
        @NotEmpty String code,
        Boolean isActive
) implements Serializable {

    public static CountryDistrictUpdateVM ofEntity(CountryDistrict entity) {
        return new CountryDistrictUpdateVM(
                entity.getId(),
                entity.getCountry() != null ? entity.getCountry().getId() : null,
                entity.getName(),
                entity.getCode(),
                entity.getIsActive()
        );
    }
}
