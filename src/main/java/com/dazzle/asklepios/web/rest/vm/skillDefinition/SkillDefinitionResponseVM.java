package com.dazzle.asklepios.web.rest.vm.skillDefinition;

import com.dazzle.asklepios.domain.SkillDefinition;

import java.io.Serializable;

public record SkillDefinitionResponseVM(
        Long id,
        String code,
        String name,
        String description,
        Boolean isActive,
        Long facilityId,
        String facilityName,
        String type
) implements Serializable {

    public static SkillDefinitionResponseVM ofEntity(SkillDefinition entity) {
        return new SkillDefinitionResponseVM(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getIsActive(),
                entity.getFacility() != null ? entity.getFacility().getId() : null,
                entity.getFacility() != null ? entity.getFacility().getName() : null,
                entity.getType()
        );
    }
}
