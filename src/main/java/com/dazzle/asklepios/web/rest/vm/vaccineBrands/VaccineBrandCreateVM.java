package com.dazzle.asklepios.web.rest.vm.vaccineBrands;

import com.dazzle.asklepios.domain.VaccineBrands;
import com.dazzle.asklepios.domain.enumeration.MeasurementUnit;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VaccineBrandCreateVM(
        @NotEmpty String name,
        @NotEmpty String code,
        @NotEmpty String manufacture,
        @NotNull BigDecimal volume,
        @NotEmpty
        String unit,
        String marketingAuthorizationHolder,
        @NotNull Long facilityId,
        @NotNull
        @Positive
        BigDecimal price,
        Boolean isActive
) implements Serializable {

    public static VaccineBrandCreateVM ofEntity(VaccineBrands brand) {
        return new VaccineBrandCreateVM(
                brand.getName(),
                brand.getCode(),
                brand.getManufacture(),
                brand.getVolume(),
                brand.getUnit(),
                brand.getMarketingAuthorizationHolder(),
                brand.getFacility() != null ? brand.getFacility().getId() : null,
                brand.getPrice(),
                brand.getIsActive()
        );
    }
}
