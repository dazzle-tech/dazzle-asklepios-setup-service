package com.dazzle.asklepios.integration.waseel.dto;

public record WaseelSbsImportResultDTO(
        Integer totalRows,
        Integer successRows,
        Integer failedRows,
        String message,
        String errorDetails
) {}
