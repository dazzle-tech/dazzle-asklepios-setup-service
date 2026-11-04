package com.dazzle.asklepios.service.dto;

public record CptConflictDTO(
        String code,
        String incomingDescription,
        String incomingCodeCategory,
        String incomingServiceCategory,
        String incomingMainCategory,
        String existingDescription,
        String existingCodeCategory,
        String existingServiceCategory,
        String existingMainCategory
) {}
