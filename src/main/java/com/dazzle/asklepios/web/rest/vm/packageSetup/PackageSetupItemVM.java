package com.dazzle.asklepios.web.rest.vm.packageSetup;

import com.dazzle.asklepios.domain.PackageSetupItem;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/** One package line. On save only itemType, sourceId, quantity and isActive are used; code and name come from the source. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PackageSetupItemVM(
        Long id,
        @NotNull PriceListItemType itemType,
        @NotNull Long sourceId,
        String itemCode,
        String itemName,
        @NotNull @Min(1) Integer quantity,
        Boolean isActive
) implements Serializable {

    public static PackageSetupItemVM ofEntity(PackageSetupItem entity) {
        return new PackageSetupItemVM(
                entity.getId(),
                entity.getItemType(),
                entity.getSourceId(),
                entity.getItemCode(),
                entity.getItemName(),
                entity.getQuantity(),
                entity.getIsActive()
        );
    }
}
