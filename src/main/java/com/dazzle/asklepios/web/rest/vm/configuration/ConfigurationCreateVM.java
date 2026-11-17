package com.dazzle.asklepios.web.rest.vm.configuration;

import com.dazzle.asklepios.domain.Configuration;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import com.dazzle.asklepios.domain.enumeration.ConfigurationReferenceType;
import com.dazzle.asklepios.domain.enumeration.ConfigurationValueType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * View Model for creating a Configuration via REST.
 * <p>
 * Scope is derived from the optional fields:
 * facility and users empty = organization,
 * facility set and users empty = facility,
 * facility and users set = user.
 */
public record ConfigurationCreateVM(
        Long facilityId,
        Set<Long> userIds,

        @NotNull ConfigurationKeys key,
        @NotEmpty String value,
        @NotNull ConfigurationValueType valueType,
        @NotNull ConfigurationReferenceType referenceType,
        @NotEmpty String description,
        Boolean isActive
) implements Serializable {

    public static ConfigurationCreateVM ofEntity(Configuration configuration) {
        Set<Long> userIds = configuration.getUsers() == null
                ? Set.of()
                : configuration.getUsers().stream().map(User::getId).collect(Collectors.toSet());
        return new ConfigurationCreateVM(
                configuration.getFacility() != null ? configuration.getFacility().getId() : null,
                userIds,
                configuration.getKey(),
                configuration.getValue(),
                configuration.getValueType(),
                configuration.getReferenceType(),
                configuration.getDescription(),
                configuration.getIsActive()
        );
    }
}
