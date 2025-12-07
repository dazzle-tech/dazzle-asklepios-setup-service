package com.dazzle.asklepios.web.rest.vm.countryDistrict;

import com.dazzle.asklepios.domain.CountryDistrict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CountryDistrictCreateVM(
        @NotEmpty String name,
        @NotEmpty String code,
        Boolean isActive
) implements Serializable {

    public static CountryDistrictCreateVM ofEntity(CountryDistrict district) {
        return new CountryDistrictCreateVM(
                district.getName(),
                district.getCode(),
                district.getIsActive()
        );
    }
}
