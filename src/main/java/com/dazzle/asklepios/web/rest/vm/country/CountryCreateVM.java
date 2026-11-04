package com.dazzle.asklepios.web.rest.vm.country;

import com.dazzle.asklepios.domain.Country;
import com.dazzle.asklepios.domain.enumeration.CountryName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CountryCreateVM(
        @NotNull CountryName name,
        @NotEmpty String code,
        Boolean isActive
) implements Serializable {

    public static CountryCreateVM ofEntity(Country country) {
        return new CountryCreateVM(
                country.getName(),
                country.getCode(),
                country.getIsActive()
        );
    }
}
