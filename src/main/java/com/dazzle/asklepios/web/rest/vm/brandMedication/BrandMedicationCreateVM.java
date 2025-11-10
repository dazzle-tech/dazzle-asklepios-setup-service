package com.dazzle.asklepios.web.rest.vm.brandMedication;

import com.dazzle.asklepios.domain.BrandMedication;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;

public record BrandMedicationCreateVM(
        @NotEmpty String name,
        String manufacturer,
        @NotEmpty String dosageForm,
        String usageInstructions,
        String storageRequirements,
        Boolean expiresAfterOpening,
        BigDecimal expiresAfterOpeningValue,
        String expiresAfterOpeningUnit,
        Boolean useSinglePatient,
        Boolean highCostMedication,
        String costCategory,
        String roa,
        Boolean isActive
) {
    public static BrandMedicationCreateVM ofEntity(BrandMedication entity) {
        if (entity == null) return null;
        return new BrandMedicationCreateVM(
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
                entity.getIsActive()
        );
    }
}
