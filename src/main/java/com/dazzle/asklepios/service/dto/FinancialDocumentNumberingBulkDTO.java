package com.dazzle.asklepios.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

public record FinancialDocumentNumberingBulkDTO(

        @NotNull
        Long facilityId,

        @NotEmpty
        @Valid
        List<FinancialDocumentNumberingDTO> configurations

) implements Serializable {
}
