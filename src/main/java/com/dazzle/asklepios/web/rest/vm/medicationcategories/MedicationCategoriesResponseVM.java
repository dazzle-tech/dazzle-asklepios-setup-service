package com.dazzle.asklepios.web.rest.vm.medicationcategories;

import com.dazzle.asklepios.domain.MedicationCategories;
import java.util.List;

public record MedicationCategoriesResponseVM(
        Long id,
        String name
) {
    public static MedicationCategoriesResponseVM mapEntity(MedicationCategories entity) {
        return new MedicationCategoriesResponseVM(
                entity.getId(),
                entity.getName()
        );
    }

    public static List<MedicationCategoriesResponseVM> mapEntityList(List<MedicationCategories> entities) {
        return entities.stream()
                .map(MedicationCategoriesResponseVM::mapEntity)
                .toList();
    }
}
