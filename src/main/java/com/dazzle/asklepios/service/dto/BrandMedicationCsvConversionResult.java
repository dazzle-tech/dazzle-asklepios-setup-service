package com.dazzle.asklepios.service.dto;

public record BrandMedicationCsvConversionResult(
        byte[] csvContent,
        int totalRecords,
        int successfulRecords,
        int skippedRecords
) {
}

