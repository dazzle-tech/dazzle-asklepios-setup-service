package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.DiscountApplicableOn;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DiscountDTO(

        Long id,

        @NotNull
        Long facilityId,

        @NotBlank
        @Size(max = 50)
        String code,

        @NotBlank
        @Size(max = 150)
        String name,

        @NotNull
        DiscountType discountType,

        BigDecimal percentage,

        BigDecimal fixedAmount,

        Currency currency,

        @NotNull
        DiscountApplicableOn applicableOn,

        @NotNull
        LocalDate validFrom,

        LocalDate validTo,

        BigDecimal maximumDiscountAmount,

        BigDecimal minimumInvoiceAmount,

        Boolean requiresReason,

        Boolean requiresApproval,

        Boolean combinable,

        Boolean isDefault,

        Boolean active,

        @Size(max = 500)
        String description

) implements Serializable {
}