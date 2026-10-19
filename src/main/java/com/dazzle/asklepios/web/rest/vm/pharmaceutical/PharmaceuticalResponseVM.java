package com.dazzle.asklepios.web.rest.vm.pharmaceutical;

import com.dazzle.asklepios.domain.Pharmaceutical;

import java.io.Serializable;

public record PharmaceuticalResponseVM(
        Long id,
        Long activeIngredientId,
        String activeIngredientName,
        String code,
        String billingCode,
        String itemDescription,
        String secondDescription,
        Boolean isActive,
        String pharmaceuticalName
) implements Serializable {

    public static PharmaceuticalResponseVM ofEntity(Pharmaceutical entity) {
        return new PharmaceuticalResponseVM(
                entity.getId(),
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getId() : null,
                entity.getActiveIngredient() != null ? entity.getActiveIngredient().getName() : null,
                entity.getCode(),
                entity.getBillingCode(),
                entity.getItemDescription(),
                entity.getSecondDescription(),
                entity.getIsActive(),
                entity.getPharmaceuticalName()
        );
    }
}
