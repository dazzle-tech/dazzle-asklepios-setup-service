package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.biling.FinancialDocumentType;

import java.io.Serializable;

public record FinancialDocumentSequenceStatusDTO(

        FinancialDocumentType documentType,

        String periodKey,

        Long lastNumber,

        Long nextNumber,

        String sampleDocumentNumber

) implements Serializable {
}
