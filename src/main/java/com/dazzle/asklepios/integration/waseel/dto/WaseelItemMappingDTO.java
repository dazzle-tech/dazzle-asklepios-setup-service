package com.dazzle.asklepios.integration.waseel.dto;

import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;

public record WaseelItemMappingDTO(
        Long id,
        BillingItemTypes itemType,
        Long sourceId,
        String itemCode,
        String itemName,
        Long sbsCatalogId,
        String waseelItemType,
        String sbsCode,
        String sbsDescription,
        Boolean requiresPreauth,
        Boolean isActive,
        String notes
) {}