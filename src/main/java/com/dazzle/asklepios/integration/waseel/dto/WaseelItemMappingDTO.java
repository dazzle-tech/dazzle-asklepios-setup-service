package com.dazzle.asklepios.integration.waseel.dto;

public record WaseelItemMappingDTO(
        Long id,
        String itemType,
        Long sourceId,
        String itemCode,
        String itemName,
        String waseelItemType,
        Long sbsCatalogId,
        String sbsCode,
        String sbsDescription,
        Boolean requiresPreauth,
        Boolean isActive,
        String notes
) {}