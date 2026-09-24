package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PriceListSetupItemDTO(

        Long id,

        Long priceListSetupId,

        Long waseelItemMappingId,

        Long sbsCatalogId,

        @NotNull
        PriceListItemType itemType,

        @NotNull
        Long sourceId,

        @NotBlank
        String itemCode,

        @Size(max = 100)
        String nonStandardCode,

        @NotBlank
        String itemName,

        String category,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal unitPrice,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("100.00")
        BigDecimal discountPercentage,

        Boolean isActive,

        Boolean requiresPreAuthorization,

        Instant createdDate,

        Instant lastModifiedDate,

        String createdBy,

        String lastModifiedBy

) {
}
