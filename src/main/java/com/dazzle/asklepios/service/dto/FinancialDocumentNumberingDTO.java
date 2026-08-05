package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.BillingConfigurationStatus;
import com.dazzle.asklepios.domain.enumeration.BillingResetFrequency;
import com.dazzle.asklepios.domain.enumeration.biling.FinancialDocumentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record FinancialDocumentNumberingDTO(

        Long id,

        @NotNull
        Long facilityId,

        @NotNull
        FinancialDocumentType documentType,

        @NotBlank
        @Size(max = 20)
        String prefix,

        @NotNull
        @Min(1)
        Integer sequenceLength,

        @NotNull
        Boolean includeYear,

        @NotNull
        Boolean includeFacilityCode,

        @NotBlank
        @Size(max = 5)
        String numberSeparator,

        @NotNull
        BillingResetFrequency resetFrequency,

        @NotNull
        @Min(1)
        Long startingNumber,

        Boolean active,

        BillingConfigurationStatus status

) implements Serializable {
}
