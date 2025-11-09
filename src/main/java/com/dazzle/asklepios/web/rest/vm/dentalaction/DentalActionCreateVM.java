package com.dazzle.asklepios.web.rest.vm.dentalaction;

import com.dazzle.asklepios.domain.DentalAction;
import com.dazzle.asklepios.domain.enumeration.DentalActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DentalActionCreateVM(
        @NotBlank String description,
        @NotNull DentalActionType type,
        String imageName,
        Boolean isActive
) {
    public DentalAction toEntity() {
        return DentalAction.builder()
                .description(description)
                .type(type)
                .imageName(imageName)
                .isActive(isActive != null ? isActive : true)
                .build();
    }
}
