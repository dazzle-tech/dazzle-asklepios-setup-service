package com.dazzle.asklepios.service.dto.discountCode;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

public record DiscountCodeAllowedUsersUpdateDTO(
        @NotNull List<@NotNull Long> userIds
) implements Serializable {
}
