package com.dazzle.asklepios.web.rest.vm.country;

import com.dazzle.asklepios.domain.Country;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CountryCreateVM(
        @NotEmpty String name,
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
