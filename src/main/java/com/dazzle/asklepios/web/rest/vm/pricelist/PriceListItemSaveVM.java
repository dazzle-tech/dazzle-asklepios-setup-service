package com.dazzle.asklepios.web.rest.vm.pricelist;

import com.dazzle.asklepios.domain.enumeration.biling.PriceListItemType;
import com.dazzle.asklepios.domain.enumeration.inventory.ProductTypes;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record PriceListItemSaveVM(
        @NotNull(message = "priceListId cannot be null")
        Long priceListId,

        @NotNull(message = "itemType cannot be null")
        PriceListItemType itemType,

        ProductTypes productType,   // optional, only for PRODUCT

        Long serviceId,       // required if SERVICE
        Long productId,       // required if PRODUCT


        @NotNull(message = "price cannot be null")
        BigDecimal price,

        Boolean discountAllowed,
        Boolean isActive
) implements Serializable {}
