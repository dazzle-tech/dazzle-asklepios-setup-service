package com.dazzle.asklepios.web.rest.vm.brandMedication.search;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.enumeration.Unit;

import java.math.BigDecimal;
import java.util.List;

public record BrandWithActivesVM(
        Long id,
        String name,
        String code,

        Boolean isActive,
        String manufacturer,
        String dosageForm,
        String usageInstructions,
        String storageRequirements,
        Boolean expiresAfterOpening,
        BigDecimal expiresAfterOpeningValue,
        Unit expiresAfterOpeningUnit,
        Boolean useSinglePatient,
        Boolean highCostMedication,
        String costCategory,
        String roa,
        Long uomGroupId,
        Long uomGroupUnitId,
        List<ActiveIngredientInBrandVM> activeIngredients
) {
    public static BrandWithActivesVM ofEntity(
            BrandMedication brand,
            List<ActiveIngredientInBrandVM> actives
    ) {
        return new BrandWithActivesVM(
                brand.getId(),
                brand.getName(),
                brand.getCode(),
                brand.getIsActive(),
                brand.getManufacturer(),
                brand.getDosageForm(),
                brand.getUsageInstructions(),
                brand.getStorageRequirements(),
                brand.getExpiresAfterOpening(),
                brand.getExpiresAfterOpeningValue(),
                brand.getExpiresAfterOpeningUnit(),
                brand.getUseSinglePatient(),
                brand.getHighCostMedication(),
                brand.getCostCategory(),
                brand.getRoa(),
                brand.getUomGroup()!=null? brand.getUomGroup().getId():null,
                brand.getUomGroupUnit()!=null?brand.getUomGroupUnit().getId():null,
                actives
        );
    }
}
