package com.dazzle.asklepios.service.dto.skillDefinition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record SkillDefinitionUpdateDTO(
        @NotNull Long id,
        @NotNull Long facilityId,
        @NotBlank String code,
        @NotBlank String name,
        String description,
        @NotBlank String type

) implements Serializable {
}
