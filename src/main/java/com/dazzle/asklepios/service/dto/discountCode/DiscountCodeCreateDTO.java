package com.dazzle.asklepios.service.dto.discountCode;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

public record DiscountCodeCreateDTO(
        @NotNull Long facilityId,
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        @NotNull DiscountType discountType,
        @DecimalMin("0") @DecimalMax("100") BigDecimal percentage,
        @DecimalMin("0") BigDecimal fixedAmount,
        Currency currency
) implements Serializable {
}
