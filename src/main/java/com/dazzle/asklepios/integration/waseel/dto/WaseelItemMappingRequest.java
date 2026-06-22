package com.dazzle.asklepios.integration.waseel.dto;


public record WaseelItemMappingRequest(
        String itemType,
        Long sourceId,
        String itemCode,
        String itemName,
        String waseelItemType,
        Long sbsCatalogId,
        Boolean requiresPreauth,
        Boolean isActive,
        String notes
) {}