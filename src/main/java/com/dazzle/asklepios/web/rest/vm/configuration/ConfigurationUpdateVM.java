package com.dazzle.asklepios.web.rest.vm.configuration;

import com.dazzle.asklepios.domain.Configuration;
import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;

/**
 * View Model for updating a Configuration via REST.
 */
public record ConfigurationUpdateVM(
        @NotNull Long id,
        Long facilityId,
        @NotNull ConfigurationKeys key,
        @NotEmpty String value,
        @NotEmpty String description,
        Boolean isIntegration
) implements Serializable {

    public static ConfigurationUpdateVM ofEntity(Configuration configuration) {
        return new ConfigurationUpdateVM(
                configuration.getId(),
                configuration.getFacility() != null ? configuration.getFacility().getId() : null,
                configuration.getKey(),
                configuration.getValue(),
                configuration.getDescription(),
                configuration.getIsIntegration()
        );
    }
}
