package com.dazzle.asklepios.web.rest.vm.medicationcategoriesclass;

import com.dazzle.asklepios.domain.MedicationCategoriesClass;
import java.util.List;

public record MedicationCategoriesClassResponseVM(
        Long id,
        String name,
        Long medicationCategoriesId
) {
    public static MedicationCategoriesClassResponseVM mapEntity(MedicationCategoriesClass entity) {
        return new MedicationCategoriesClassResponseVM(
                entity.getId(),
                entity.getName(),
                entity.getMedicationCategoriesId()
        );
    }

    public static List<MedicationCategoriesClassResponseVM> mapEntityList(List<MedicationCategoriesClass> entities) {
        return entities.stream()
                .map(MedicationCategoriesClassResponseVM::mapEntity)
                .toList();
    }
}