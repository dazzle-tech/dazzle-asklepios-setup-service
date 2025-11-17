package com.dazzle.asklepios.web.rest.vm.dentalaction;

import com.dazzle.asklepios.domain.DentalAction;
import com.dazzle.asklepios.domain.enumeration.DentalActionType;

public record DentalActionResponseVM(
        Long id,
        String description,
        DentalActionType type,
        String imageName,
        Boolean isActive
) {
    public static DentalActionResponseVM fromEntity(DentalAction e) {
        return new DentalActionResponseVM(
                e.getId(),
                e.getDescription(),
                e.getType(),
                e.getImageName(),
                e.getIsActive()
        );
    }
}
