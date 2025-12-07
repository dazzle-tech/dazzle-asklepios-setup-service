package com.dazzle.asklepios.web.rest.vm.brandMedication;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.enumeration.Unit;
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
        Unit expiresAfterOpeningUnit,
        Boolean useSinglePatient,
        Boolean highCostMedication,
        String costCategory,
        String roa,
        Boolean isActive,
        String code,
        @NotNull Long uomGroupId,
        @NotNull Long uomGroupUnitId
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
                entity.getCode(),
                entity.getUomGroup()!=null? entity.getUomGroup().getId():null,
                entity.getUomGroupUnit()!=null?entity.getUomGroupUnit().getId():null
        );
    }
}
