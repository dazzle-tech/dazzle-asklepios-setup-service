package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.BillingTrigger;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record BillingRuleDTO(

        Long id,

        @NotBlank
        @Size(max = 150)
        String name,

        @NotNull
        BillingItemTypes billingItemType,

        @NotNull
        BillingTrigger billingTrigger,

        Boolean isDefault

) implements Serializable {
}