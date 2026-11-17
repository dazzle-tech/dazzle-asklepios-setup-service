package com.dazzle.asklepios.web.rest.vm.dentaltoothnumberingmethod;

import com.dazzle.asklepios.domain.DentalToothNumberingMethod;

import java.util.Map;

public record DentalToothNumberingMethodResponseVM(
        Long id,
        String name,
        String description,
        Map<String, String> toothLabels,
        Boolean isActive
) {
    public static DentalToothNumberingMethodResponseVM fromEntity(DentalToothNumberingMethod entity) {
        return new DentalToothNumberingMethodResponseVM(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getToothLabelMap(),
                entity.getIsActive()
        );
    }
}
