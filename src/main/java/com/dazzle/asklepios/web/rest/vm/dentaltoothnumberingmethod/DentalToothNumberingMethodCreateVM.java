package com.dazzle.asklepios.web.rest.vm.dentaltoothnumberingmethod;

import com.dazzle.asklepios.domain.DentalToothNumberingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record DentalToothNumberingMethodCreateVM(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description,
        Map<String, String> toothLabels,
        Boolean isActive
) {
    public DentalToothNumberingMethod toEntity() {
        DentalToothNumberingMethod entity = DentalToothNumberingMethod.builder()
                .name(name != null ? name.trim() : null)
                .description(description)
                .isActive(isActive != null ? isActive : Boolean.TRUE)
                .build();
        entity.setToothLabelMap(toothLabels);
        return entity;
    }
}
