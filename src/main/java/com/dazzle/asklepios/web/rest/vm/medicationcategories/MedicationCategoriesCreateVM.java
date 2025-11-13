package com.dazzle.asklepios.web.rest.vm.medicationcategories;


import com.dazzle.asklepios.domain.MedicationCategories;

public record MedicationCategoriesCreateVM(
        String name
) {
    public MedicationCategories mapEntity() {
        return MedicationCategories.builder()
                .name(name)
                .build();
    }
}
