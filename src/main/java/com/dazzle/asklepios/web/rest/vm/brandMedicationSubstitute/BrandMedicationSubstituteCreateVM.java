package com.dazzle.asklepios.web.rest.vm.brandMedicationSubstitute;

import com.dazzle.asklepios.domain.BrandMedicationSubstitute;
import jakarta.validation.constraints.NotNull;

public record BrandMedicationSubstituteCreateVM(
        @NotNull Long brandId,
        @NotNull Long alternativeBrandId
) {
    public static BrandMedicationSubstituteCreateVM ofEntity(BrandMedicationSubstitute entity) {
        if (entity == null) return null;
        return new BrandMedicationSubstituteCreateVM(
                entity.getBrandMedication() != null ? entity.getBrandMedication().getId() : null,
                entity.getAlternativeBrandMedication() != null ? entity.getAlternativeBrandMedication().getId() : null
        );
    }
}
