package com.dazzle.asklepios.web.rest.vm.country;

import com.dazzle.asklepios.domain.Country;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CountryUpdateVM(
        @NotNull Long id,
        @NotEmpty String name,
        @NotEmpty String code,
        Boolean isActive
) implements Serializable {

    public static CountryUpdateVM ofEntity(Country country) {
        return new CountryUpdateVM(
                country.getId(),
                country.getName(),
                country.getCode(),
                country.getIsActive()
        );
    }
}
