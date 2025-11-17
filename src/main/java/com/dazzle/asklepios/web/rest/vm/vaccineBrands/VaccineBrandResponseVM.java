package com.dazzle.asklepios.web.rest.vm.vaccineBrands;

import com.dazzle.asklepios.domain.VaccineBrands;
import com.dazzle.asklepios.domain.enumeration.MeasurementUnit;
import java.io.Serializable;
import java.math.BigDecimal;

public record VaccineBrandResponseVM(
        Long id,
        String name,
        String manufacture,
        BigDecimal volume,
        MeasurementUnit unit,
        String marketingAuthorizationHolder,
        Boolean isActive
) implements Serializable {

    public static VaccineBrandResponseVM ofEntity(VaccineBrands brand) {
        return new VaccineBrandResponseVM(
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
