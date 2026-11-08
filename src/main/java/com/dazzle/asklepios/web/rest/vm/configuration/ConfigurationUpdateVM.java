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
 * View Model for updating a Configuration via REST.
 * {@code facilityId} and {@code userIds} are the desired scope, including empty.
 */
public record ConfigurationUpdateVM(
        @NotNull Long id,
        Long facilityId,
        Set<Long> userIds,
        @NotNull ConfigurationKeys key,
        @NotEmpty String value,
        @NotNull ConfigurationValueType valueType,
        @NotNull ConfigurationReferenceType referenceType,
        @NotEmpty String description,
        @NotNull Boolean isActive
) implements Serializable {

    public static ConfigurationUpdateVM ofEntity(Configuration configuration) {
        Set<Long> userIds = configuration.getUsers() == null
                ? Set.of()
                : configuration.getUsers().stream().map(User::getId).collect(Collectors.toSet());
        return new ConfigurationUpdateVM(
                configuration.getId(),
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
