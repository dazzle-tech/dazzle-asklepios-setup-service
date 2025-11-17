package com.dazzle.asklepios.web.rest.vm.vaccineBrands;

import com.dazzle.asklepios.domain.VaccineBrands;
import com.dazzle.asklepios.domain.enumeration.MeasurementUnit;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record VaccineBrandUpdateVM(
        @NotNull Long id,
        @NotEmpty String name,
        @NotEmpty String manufacture,
        @NotNull BigDecimal volume,
        @NotNull MeasurementUnit unit,
        String marketingAuthorizationHolder,
        @NotNull Boolean isActive
) implements Serializable {

    public static VaccineBrandUpdateVM ofEntity(VaccineBrands brand) {
        return new VaccineBrandUpdateVM(
                brand.getId(),
                brand.getName(),
                brand.getManufacture(),
                brand.getVolume(),
                brand.getUnit(),
                brand.getMarketingAuthorizationHolder(),
                brand.getIsActive()
        );
    }
}
