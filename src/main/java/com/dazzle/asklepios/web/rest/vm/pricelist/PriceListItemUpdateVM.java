package com.dazzle.asklepios.web.rest.vm.pricelist;

import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PriceListItemUpdateVM(
        @NotNull(message = "id cannot be null")
        Long id,

        @NotNull(message = "priceListId cannot be null")
        Long priceListId,

        @NotNull(message = "itemType cannot be null")
        BillingItemTypes itemType,

        Long serviceId,

        Long brandMedicationId,

        Long diagnosticTestId,

        Long procedureId,

        @NotNull(message = "price cannot be null")
        BigDecimal price,

        Boolean discountAllowed,
        Boolean isActive
) implements Serializable {}
