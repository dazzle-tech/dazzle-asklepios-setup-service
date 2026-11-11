package com.dazzle.asklepios.service.dto.discountCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record DiscountCodeVerifyUserDTO(
        @NotBlank
        @Size(max = 50)
        String login,

        @NotBlank
        @Size(max = 100)
        String password
) implements Serializable {
}
