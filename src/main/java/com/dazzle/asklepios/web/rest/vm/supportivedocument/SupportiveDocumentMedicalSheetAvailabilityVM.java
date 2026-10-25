package com.dazzle.asklepios.web.rest.vm.supportivedocument;

/**
 * Availability of one MedicalSheets enum value in Supportive Document Setup.
 * Unsupported sheets are shown disabled; they are never persisted as requirement codes.
 */
public record SupportiveDocumentMedicalSheetAvailabilityVM(
        String medicalSheetCode,
        String medicalSheetName,
        String medicalSheetPath,
        boolean supported,
        String unsupportedReason,
        int displayOrder
) {
}
