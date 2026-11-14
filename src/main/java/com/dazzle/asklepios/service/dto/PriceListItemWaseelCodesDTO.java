package com.dazzle.asklepios.service.dto;

/**
 * Waseel codes of an insurance price-list item. The SBS fields come from the SBS catalog
 * entry the item is linked to, so an item without a Waseel Item Mapping still has its
 * NPHIES code description.
 */
public record PriceListItemWaseelCodesDTO(
        String itemCode,
        String nonStandardCode,
        String sbsCode,
        String sbsDescription,
        String waseelItemType
) {

    public static final PriceListItemWaseelCodesDTO EMPTY = new PriceListItemWaseelCodesDTO(null, null, null, null, null);
}
