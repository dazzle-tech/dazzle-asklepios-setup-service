package com.dazzle.asklepios.service.dto;

public record LoincConflictDTO(
        String code,
        String incomingDescription,
        String incomingCategory,
        String existingDescription,
        String existingCategory
) {}
