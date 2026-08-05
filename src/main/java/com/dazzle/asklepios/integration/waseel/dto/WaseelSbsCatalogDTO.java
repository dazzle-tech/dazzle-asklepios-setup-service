package com.dazzle.asklepios.integration.waseel.dto;

public record WaseelSbsCatalogDTO(
        Long id,
        String waseelItemType,
        String sbsCode,
        String updateType,
        String revisionDetails,
        String shortDescription,
        String longDescription,
        Boolean isActive
) {}