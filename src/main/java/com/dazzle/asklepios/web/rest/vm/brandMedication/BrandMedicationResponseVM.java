package com.dazzle.asklepios.web.rest.vm.brandMedication;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.enumeration.BrandMedicationUnit;

import java.math.BigDecimal;

public record BrandMedicationResponseVM(
        Long id,
        String name,
        String manufacturer,
        String dosageForm,
        String usageInstructions,
        String storageRequirements,
        Boolean expiresAfterOpening,
        BigDecimal expiresAfterOpeningValue,
        BrandMedicationUnit expiresAfterOpeningUnit,
        Boolean useSinglePatient,
        Boolean highCostMedication,
        String costCategory,
        String roa,
        Boolean isActive,
        String code
        //       , Long uomGroup,
//        Long uomGroupUnit
) {
    public static BrandMedicationResponseVM ofEntity(BrandMedication entity) {
        if (entity == null) return null;
        return new BrandMedicationResponseVM(
                entity.getId(),
                entity.getName(),
                entity.getManufacturer(),
                entity.getDosageForm(),
                entity.getUsageInstructions(),
                entity.getStorageRequirements(),
                entity.getExpiresAfterOpening(),
                entity.getExpiresAfterOpeningValue(),
                entity.getExpiresAfterOpeningUnit(),
                entity.getUseSinglePatient(),
                entity.getHighCostMedication(),
                entity.getCostCategory(),
                entity.getRoa(),
                entity.getIsActive(),
                entity.getCode()
        );
    }
}
