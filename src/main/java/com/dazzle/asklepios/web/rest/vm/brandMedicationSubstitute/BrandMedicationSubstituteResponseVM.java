package com.dazzle.asklepios.web.rest.vm.brandMedicationSubstitute;

import com.dazzle.asklepios.domain.BrandMedicationSubstitute;

public record BrandMedicationSubstituteResponseVM(
        Long id,
        Long brandId,
        Long alternativeBrandId
) {
    public static BrandMedicationSubstituteResponseVM ofEntity(BrandMedicationSubstitute entity) {
        if (entity == null) return null;
        return new BrandMedicationSubstituteResponseVM(
                entity.getId(),
                entity.getBrandMedication() != null ? entity.getBrandMedication().getId() : null,
                entity.getAlternativeBrandMedication() != null ? entity.getAlternativeBrandMedication().getId() : null
        );
    }
}
