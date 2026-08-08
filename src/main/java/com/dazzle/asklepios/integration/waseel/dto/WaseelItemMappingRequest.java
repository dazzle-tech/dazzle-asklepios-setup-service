package com.dazzle.asklepios.integration.waseel.dto;


import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;

public record WaseelItemMappingRequest(
        BillingItemTypes itemType,
        Long sourceId,
        String itemCode,
        String itemName,
        Long sbsCatalogId,
        Boolean isActive,
        String notes
) {}