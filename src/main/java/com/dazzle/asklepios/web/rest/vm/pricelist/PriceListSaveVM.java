package com.dazzle.asklepios.web.rest.vm.pricelist;

import com.dazzle.asklepios.domain.enumeration.biling.PriceListTypes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record PriceListSaveVM(
        Long id,                 // null=create bulk, not null=update single
        List<Long> facilityIds,  // bulk create list
        @NotBlank(message = "Name cannot be null")
        String name,
        @NotNull(message = "Type cannot be null")
        PriceListTypes type,
        @NotNull(message = "Effective from cannot be null")
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String description,
        Boolean isActive
) implements Serializable {}
