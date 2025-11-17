package com.dazzle.asklepios.web.rest.vm.medicationcategories;

import com.dazzle.asklepios.domain.MedicationCategories;

public record MedicationCategoriesUpdateVM(
        Long id,
        String name
) {
    public MedicationCategories mapEntity() {
        return MedicationCategories.builder()
                .id(id)
                .name(name)
                .build();
    }
}
