package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.biling.FinancialDocumentType;

import java.io.Serializable;

public record FinancialDocumentNumberResponse(

        String documentNumber,

        Long sequenceNumber,

        String periodKey,

        FinancialDocumentType documentType,

        Long facilityId

) implements Serializable {
}
