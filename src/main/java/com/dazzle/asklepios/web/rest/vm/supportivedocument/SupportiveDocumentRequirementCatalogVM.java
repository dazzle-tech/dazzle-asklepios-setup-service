package com.dazzle.asklepios.web.rest.vm.supportivedocument;

import java.util.List;

/**
 * Catalog entry for one requirement code.
 * {@code medicalSheetCode}/{@code medicalSheetName}/{@code medicalSheetPath} remain the primary
 * group (backward compatible). {@code additionalMedicalSheets} lists further groups that display
 * the same code without alias keys.
 */
public record SupportiveDocumentRequirementCatalogVM(
        String code,
        String label,
        String medicalSheetCode,
        String medicalSheetName,
        String medicalSheetPath,
        int displayOrder,
        List<SupportiveDocumentMedicalSheetRefVM> additionalMedicalSheets
) {
}
