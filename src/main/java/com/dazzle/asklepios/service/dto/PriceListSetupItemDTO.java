package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

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

        @NotBlank
        String itemName,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal unitPrice,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("100.00")
        BigDecimal discountPercentage,

        Boolean isActive

) {
}