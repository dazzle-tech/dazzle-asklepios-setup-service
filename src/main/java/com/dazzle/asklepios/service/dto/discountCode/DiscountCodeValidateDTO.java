package com.dazzle.asklepios.service.dto.discountCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record DiscountCodeValidateDTO(
        @NotNull
        Long facilityId,

        @NotBlank
        @Size(max = 50)
        String code,

        @NotNull
        Long userId,

        @NotBlank
        @Size(max = 300)
        String authorizationToken
) implements Serializable {
}
