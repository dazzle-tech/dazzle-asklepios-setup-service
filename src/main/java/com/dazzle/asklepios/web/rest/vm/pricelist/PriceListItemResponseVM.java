package com.dazzle.asklepios.web.rest.vm.pricelist;

import com.dazzle.asklepios.domain.PriceListItem;
import com.dazzle.asklepios.domain.enumeration.biling.PriceListItemType;
import com.dazzle.asklepios.domain.enumeration.inventory.ProductTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record PriceListItemResponseVM(
        Long id,
        Long priceListId,
        PriceListItemType itemType,
        ProductTypes productType,
        Long serviceId,
        Long productId,

        BigDecimal price,
        Boolean discountAllowed,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) implements Serializable {

    public static PriceListItemResponseVM ofEntity(PriceListItem p) {
        return new PriceListItemResponseVM(
                p.getId(),
                p.getPriceListId(),
                p.getItemType(),
                p.getProductType(),
                p.getServiceId(),
                p.getProductId(),

                p.getPrice(),
                p.getDiscountAllowed(),
                p.getIsActive(),
                p.getCreatedDate(),
                p.getLastModifiedDate()
        );
    }
}
