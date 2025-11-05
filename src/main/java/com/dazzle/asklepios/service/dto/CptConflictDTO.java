package com.dazzle.asklepios.service.dto;

public record CptConflictDTO(
        String code,
        String incomingDescription,
        String incomingCategory,
        String existingDescription,
        String existingCategory
) {}
