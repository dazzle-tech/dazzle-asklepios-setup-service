package com.dazzle.asklepios.web.rest.vm.configuration;

import com.dazzle.asklepios.domain.Configuration;
import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import com.dazzle.asklepios.domain.enumeration.ConfigurationReferenceType;
import com.dazzle.asklepios.domain.enumeration.ConfigurationValueType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * View Model for creating a Configuration via REST.
 */
public record ConfigurationCreateVM(
        Long facilityId,

        @NotNull ConfigurationKeys key,
        @NotEmpty String value,
        @NotNull ConfigurationValueType valueType,
        @NotNull ConfigurationReferenceType referenceType,
        @NotEmpty String description,
        Boolean isActive
) implements Serializable {

    public static ConfigurationCreateVM ofEntity(Configuration configuration) {
        return new ConfigurationCreateVM(
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
