package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.InventoryType;
import com.dazzle.asklepios.domain.enumeration.ProductTypes;
import com.dazzle.asklepios.domain.enumeration.TimeUnit;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record InventoryProductsUpdateDTO(
        @NotNull Long id,
        @NotEmpty String name,
        @NotNull ProductTypes type,
        String code,
        String barcode,
        Long brandId,
        Long uomGroupId,
        @NotEmpty String baseUom,
        String dispenseUom,
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
) implements Serializable {}