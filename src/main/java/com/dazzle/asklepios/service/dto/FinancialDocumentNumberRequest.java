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

        LocalDate documentDate,

        /**
         * Highest sequence already issued in downstream documents for the current period.
         * When the setup counter is behind (for example after migration), allocation
         * advances to {@code minimumUsedSequence + 1} instead of reusing an old value.
         */
        Long minimumUsedSequence

) implements Serializable {
}
