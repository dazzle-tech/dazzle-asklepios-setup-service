package com.dazzle.asklepios.web.rest.vm.configuration;

import com.dazzle.asklepios.domain.Configuration;
import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import com.dazzle.asklepios.domain.enumeration.ConfigurationReferenceType;
import com.dazzle.asklepios.domain.enumeration.ConfigurationValueType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * View Model for updating a Configuration via REST.
 */
public record ConfigurationUpdateVM(
        @NotNull Long id,
        Long facilityId,
        @NotNull ConfigurationKeys key,
        @NotEmpty @Size(max = 50) String value,
        @NotEmpty @Size(max = 50) ConfigurationValueType valueType,
        @NotEmpty @Size(max = 50) ConfigurationReferenceType referenceType,
        @NotEmpty String description,
        @NotNull  Boolean isActive
) implements Serializable {

    public static ConfigurationUpdateVM ofEntity(Configuration configuration) {
        return new ConfigurationUpdateVM(
                configuration.getId(),
                configuration.getFacility() != null ? configuration.getFacility().getId() : null,
                configuration.getKey(),
                configuration.getValue(),
                configuration.getValueType(),
                configuration.getReferenceType(),
                configuration.getDescription(),
                configuration.getIsActive()
        );
    }
}
