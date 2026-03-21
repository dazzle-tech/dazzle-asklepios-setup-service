package com.dazzle.asklepios.web.rest.vm.inventoryProducts;

import com.dazzle.asklepios.domain.InventoryProducts;
import com.dazzle.asklepios.domain.enumeration.InventoryType;
import com.dazzle.asklepios.domain.enumeration.ProductTypes;
import com.dazzle.asklepios.domain.enumeration.TimeUnit;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record InventoryProductsResponseVM(
        Long Id,
        String name,
        ProductTypes type,
        String code,
        String barcode,
        Long brandId,
        Long uomGroupId,
        Long baseUom,
        Long dispenseUom,
        Boolean controlledSubstance,
        String hazardousBiohazardousTag,
        Boolean allergyRisk,
        BigDecimal itemAverageCost,
        BigDecimal pricePerBaseUom,
        Instant warrantyStartDate,
        Instant warrantyEndDate,
        BigDecimal maintenanceSchedule,
        String maintenanceScheduleType,
        Boolean criticalEquipment,
        Boolean calibrationRequired,
        Boolean trainingRequired,
        Boolean batchManaged,
        Boolean expiryDateMandatory,
        Boolean reusable,
        InventoryType inventoryType,
        Integer shelfLife,
        TimeUnit shelfLifeUnit,
        Integer leadTime,
        TimeUnit leadTimeUnit,
        String erpIntegrationId,
        Boolean isActive
) implements Serializable {
    public static InventoryProductsResponseVM ofEntity(InventoryProducts entity) {
        return new InventoryProductsResponseVM(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getCode(),
                entity.getBarcode(),
                entity.getBrandId(),
                entity.getUomGroupId(),
                entity.getBaseUom(),
                entity.getDispenseUom(),
                entity.getControlledSubstance(),
                entity.getHazardousBiohazardousTag(),
                entity.getAllergyRisk(),
                entity.getItemAverageCost(),
                entity.getPricePerBaseUom(),
                entity.getWarrantyStartDate(),
                entity.getWarrantyEndDate(),
                entity.getMaintenanceSchedule(),
                entity.getMaintenanceScheduleType(),
                entity.getCriticalEquipment(),
                entity.getCalibrationRequired(),
                entity.getTrainingRequired(),
                entity.getBatchManaged(),
                entity.getExpiryDateMandatory(),
                entity.getReusable(),
                entity.getInventoryType(),
                entity.getShelfLife(),
                entity.getShelfLifeUnit(),
                entity.getLeadTime(),
                entity.getLeadTimeUnit(),
                entity.getErpIntegrationId(),
                entity.getIsActive()
        );
    }
}