package com.dazzle.asklepios.web.rest.vm.policyDefinition;

import com.dazzle.asklepios.domain.PolicyDefinition;

import java.io.Serializable;

public record PolicyDefinitionResponseVM(
        Long id,
        String code,
        String name,
        String description,
        Boolean isActive,
        Long facilityId,
        String facilityName
) implements Serializable {

    public static PolicyDefinitionResponseVM ofEntity(PolicyDefinition entity) {
        return new PolicyDefinitionResponseVM(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getIsActive(),
                entity.getFacility() != null ? entity.getFacility().getId() : null,
                entity.getFacility() != null ? entity.getFacility().getName() : null
        );
    }
}
