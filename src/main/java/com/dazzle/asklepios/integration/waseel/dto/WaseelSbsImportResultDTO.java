package com.dazzle.asklepios.integration.waseel.dto;

public record WaseelSbsImportResultDTO(
       Long  totalRows,
       Long successRows,
       Long failedRows,
        String message,
        String errorDetails
) {}
