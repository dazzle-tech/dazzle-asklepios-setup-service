package com.dazzle.asklepios.web.rest.vm.medicationcategoriesclass;

import com.dazzle.asklepios.domain.MedicationCategoriesClass;

public record MedicationCategoriesClassUpdateVM(
        Long id,
        String name,
        Long medicationCategoriesId
) {
    public MedicationCategoriesClass mapEntity() {
        return MedicationCategoriesClass.builder()
                .id(id)
                .name(name)
                .medicationCategoriesId(medicationCategoriesId)
                .build();
    }
}
