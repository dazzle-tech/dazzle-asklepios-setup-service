package com.dazzle.asklepios.service.dto.PolicyDefinition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record PolicyDefinitionUpdateDTO(
        @NotNull Long id,
        @NotNull Long facilityId,
        @NotBlank String code,
        @NotBlank String name,
        String description,
        @NotNull Boolean isActive
)implements Serializable {}
