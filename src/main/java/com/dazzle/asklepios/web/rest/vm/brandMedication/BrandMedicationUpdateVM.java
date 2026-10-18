package com.dazzle.asklepios.web.rest.vm.brandMedication;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.enumeration.MedDistributeArea;
import com.dazzle.asklepios.domain.enumeration.Unit;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record BrandMedicationUpdateVM(
        @NotNull Long id,
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
       @NotNull Long uomGroupUnitId,
        @NotNull BigDecimal price,
        String currency,
        Long billingRuleId,
        @NotNull Long facilityId,
        @NotNull Long priceBaseUomId,
        String registrationNumber,
        String integrationCode,
        String gtinCode,
        String descriptionCode,
        List<MedDistributeArea> medDistributeArea,
        Boolean localMarket
) {
    public static BrandMedicationUpdateVM ofEntity(BrandMedication entity) {
        if (entity == null) return null;
        return new BrandMedicationUpdateVM(
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
                entity.getCode(),
                entity.getUomGroup()!=null? entity.getUomGroup().getId():null,
                entity.getUomGroupUnit()!=null?entity.getUomGroupUnit().getId():null,
                entity.getPrice(),
                entity.getCurrency(),
                entity.getBillingRule() != null
                        ? entity.getBillingRule().getId()
                        : null,
                entity.getFacility() != null ? entity.getFacility().getId() : null,
                entity.getPriceBaseUom() != null ? entity.getPriceBaseUom().getId() : null,
                entity.getRegistrationNumber(),
                entity.getIntegrationCode(),
                entity.getGtinCode(),
                entity.getDescriptionCode(),
                entity.getMedDistributeArea(),
                entity.getLocalMarket()
        );
    }
}
