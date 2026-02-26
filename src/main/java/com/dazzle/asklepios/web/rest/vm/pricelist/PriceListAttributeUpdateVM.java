package com.dazzle.asklepios.web.rest.vm.pricelist;

import com.dazzle.asklepios.domain.enumeration.biling.PriceAttributes;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PriceListAttributeUpdateVM(
        @NotNull Long id,
        @NotNull Long priceListId,
        @NotNull PriceAttributes attributeType,
        @NotEmpty String attribute,
        @NotNull BigDecimal price,
        Boolean isActive
) {}
