package com.dazzle.asklepios.web.rest.vm.pricelist;

import com.dazzle.asklepios.domain.PriceListAttribute;
import com.dazzle.asklepios.domain.enumeration.biling.PriceAttributes;

import java.math.BigDecimal;

public record PriceListAttributeResponseVM(
        Long id,
        Long priceListId,
        PriceAttributes attributeType,
        String attribute,
        BigDecimal price,
        Boolean isActive
) {
    public static PriceListAttributeResponseVM ofEntity(PriceListAttribute e) {
        if (e == null) return null;
        return new PriceListAttributeResponseVM(
                e.getId(),
                e.getPriceListId(),
                e.getAttributeType(),
                e.getAttribute(),
                e.getPrice(),
                e.getIsActive()
        );
    }
}
