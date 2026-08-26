package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.time.LocalDate;

public record PriceListSetupDTO(

        Long id,

        @NotNull
        Long facilityId,

        String facilityName,

        Boolean appliesToAllFacilities,

        @NotNull
        PriceListSetupType type,

        Long payerId,

        String payerName,

        Long nphiesPayerId,

        String nphiesPayerName,

        @NotBlank
        String name,

        String shortName,

        String description,

        @Positive
        Integer versionNumber,

        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        @NotNull
        Currency currency,

        PriceListSetupStatus status,

        Long taxId,

        String taxName,

        Instant createdDate,

        Instant lastModifiedDate,

        String createdBy,

        String lastModifiedBy

) {
}
