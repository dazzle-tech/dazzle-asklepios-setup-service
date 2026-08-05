package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.TaxApplicableOn;
import com.dazzle.asklepios.domain.enumeration.TaxCalculationType;
import com.dazzle.asklepios.domain.enumeration.TaxType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TaxDTO(

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
        TaxType taxType,

        BigDecimal percentage,

        BigDecimal fixedAmount,

        Currency currency,

        @NotNull
        TaxCalculationType calculationType,

        @NotNull
        TaxApplicableOn applicableOn,

        @NotNull
        LocalDate validFrom,

        LocalDate validTo,

        Boolean isDefault,

        Boolean active,

        @Size(max = 500)
        String description

) {

}