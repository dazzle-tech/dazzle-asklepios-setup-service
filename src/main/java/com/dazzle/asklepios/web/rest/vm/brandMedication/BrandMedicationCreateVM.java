package com.dazzle.asklepios.web.rest.vm.brandMedication;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.enumeration.BrandMedicationUnit;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BrandMedicationCreateVM(
        @NotEmpty String name,
        String manufacturer,
        @NotEmpty String dosageForm,
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
//       ,@NotNull Long uomGroup,
//       @NotNull Long uomGroupUnit
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
                entity.getIsActive(),
                entity.getCode()
        );
    }
}
