package com.dazzle.asklepios.service.dto;

public record CdtServiceMappingSyncResultDTO(
        Integer beforeCount,
        Integer added,
        Integer removed,
        Integer afterCount
) {}
