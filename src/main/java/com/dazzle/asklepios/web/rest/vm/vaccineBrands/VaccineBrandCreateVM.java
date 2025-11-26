package com.dazzle.asklepios.web.rest.vm.vaccineBrands;

import com.dazzle.asklepios.domain.VaccineBrands;
import com.dazzle.asklepios.domain.enumeration.MeasurementUnit;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VaccineBrandCreateVM(
        @NotEmpty String name,
        @NotEmpty String manufacture,
        @NotNull BigDecimal volume,
        @NotNull MeasurementUnit unit,
        String marketingAuthorizationHolder,
        Boolean isActive
) implements Serializable {

    public static VaccineBrandCreateVM ofEntity(VaccineBrands brand) {
        return new VaccineBrandCreateVM(
                brand.getName(),
                brand.getManufacture(),
                brand.getVolume(),
                brand.getUnit(),
                brand.getMarketingAuthorizationHolder(),
                brand.getIsActive()
        );
    }
}
