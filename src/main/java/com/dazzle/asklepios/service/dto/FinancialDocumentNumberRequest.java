package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.biling.FinancialDocumentType;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;

public record FinancialDocumentNumberRequest(

        @NotNull
        Long facilityId,

        @NotNull
        FinancialDocumentType documentType,

        LocalDate documentDate

) implements Serializable {
}
