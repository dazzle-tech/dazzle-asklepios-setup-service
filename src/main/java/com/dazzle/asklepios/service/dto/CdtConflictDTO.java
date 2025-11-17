package com.dazzle.asklepios.service.dto;

public record CdtConflictDTO(
        String code,
        String incomingDescription,
        String incomingClass,
        Boolean incomingIsActive,
        String existingDescription,
        String existingClass,
        Boolean existingIsActive
) {}