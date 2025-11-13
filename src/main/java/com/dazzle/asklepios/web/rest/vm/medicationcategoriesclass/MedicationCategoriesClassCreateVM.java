package com.dazzle.asklepios.web.rest.vm.medicationcategoriesclass;


import com.dazzle.asklepios.domain.MedicationCategoriesClass;

public record MedicationCategoriesClassCreateVM(
        String name,
        Long medicationCategoriesId
) {
    public MedicationCategoriesClass mapEntity() {
        return MedicationCategoriesClass.builder()
                .name(name)
                .medicationCategoriesId(medicationCategoriesId)
                .build();
    }
}