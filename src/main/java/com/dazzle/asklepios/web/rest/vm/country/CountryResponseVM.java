package com.dazzle.asklepios.web.rest.vm.country;

import com.dazzle.asklepios.domain.Country;

import java.io.Serializable;

public record CountryResponseVM(
        Long id,
        String name,
        String code,
        Boolean isActive
) implements Serializable {

    public static CountryResponseVM ofEntity(Country country) {
        return new CountryResponseVM(
                country.getId(),
                country.getName(),
                country.getCode(),
                country.getIsActive()
        );
    }
}
