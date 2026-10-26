package com.dazzle.asklepios.web.rest.vm.vaccineBrands;

import com.dazzle.asklepios.domain.VaccineBrands;
import com.dazzle.asklepios.domain.enumeration.MeasurementUnit;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * View Model for updating a VaccineBrand via REST.
 */
public record VaccineBrandUpdateVM(
        @NotNull Long id,
        @NotEmpty String name,
        @NotEmpty String manufacture,
        @NotNull BigDecimal volume,
        @NotEmpty
        String unit,
        String marketingAuthorizationHolder,
        @NotNull Long facilityId,
        @NotNull
        @Positive
        BigDecimal price,
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
                brand.getFacility() != null ? brand.getFacility().getId() : null,
                brand.getPrice(),
                brand.getIsActive()
        );
    }
}
