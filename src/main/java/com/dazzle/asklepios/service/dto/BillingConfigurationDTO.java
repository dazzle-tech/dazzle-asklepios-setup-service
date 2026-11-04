package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.BillingConfigurationKey;
import com.dazzle.asklepios.domain.enumeration.BillingConfigurationStatus;
import com.dazzle.asklepios.domain.enumeration.BillingConfigurationValueType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record BillingConfigurationDTO(

        Long id,

        @NotNull
        Long facilityId,

        @NotNull
        BillingConfigurationKey configurationKey,

        @NotNull
        BillingConfigurationValueType valueType,

        String configurationValue,

        @Size(max = 255)
        String enumCode,

        @Size(max = 500)
        String description,

        Boolean active,

        BillingConfigurationStatus status

) implements Serializable {
}