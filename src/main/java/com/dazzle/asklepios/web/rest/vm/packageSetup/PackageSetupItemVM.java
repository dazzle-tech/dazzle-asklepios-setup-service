package com.dazzle.asklepios.web.rest.vm.packageSetup;

import com.dazzle.asklepios.domain.PackageSetupItem;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * One package line: either one item (sourceId) or a whole item type (isCategory, no sourceId).
 * On save code and name come from the source; maxRepeat is for category lines only.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PackageSetupItemVM(
        Long id,
        @NotNull PriceListItemType itemType,
        Long sourceId,
        String itemCode,
        String itemName,
        @NotNull @Min(1) Integer quantity,
        Boolean isActive,
        Boolean isCategory,
        @Min(1) Integer maxRepeat,
        Long discountId
) implements Serializable {

    public static PackageSetupItemVM ofEntity(PackageSetupItem entity) {
        return new PackageSetupItemVM(
                entity.getId(),
                entity.getItemType(),
                entity.getSourceId(),
                entity.getItemCode(),
                entity.getItemName(),
                entity.getQuantity(),
                entity.getIsActive(),
                entity.getIsCategory(),
                entity.getMaxRepeat(),
                entity.getDiscountId()
        );
    }
}
