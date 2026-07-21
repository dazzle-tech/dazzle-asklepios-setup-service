package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record PriceListSetupDTO(

        Long id,

        @NotNull
        Long facilityId,

        @NotNull
        PriceListSetupType type,

        Long payerId,

        @NotBlank
        String name,

        String description,

        @NotNull
        @Positive
        Integer versionNumber,

        @NotNull
        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        @NotNull
        Currency currency

) {
}