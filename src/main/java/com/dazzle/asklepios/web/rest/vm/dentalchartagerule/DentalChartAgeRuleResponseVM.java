package com.dazzle.asklepios.web.rest.vm.dentalchartagerule;

import com.dazzle.asklepios.domain.DentalChartAgeRule;
import com.dazzle.asklepios.domain.enumeration.DentalChartType;
import com.dazzle.asklepios.domain.enumeration.ToothNumberingSystem;

import java.util.List;

public record DentalChartAgeRuleResponseVM(
        Long id,
        String name,
        Integer ageFrom,
        Integer ageTo,
        DentalChartType chartType,
        ToothNumberingSystem numberingSystem,
        List<String> toothNumbers,
        Boolean isActive
) {
    public static DentalChartAgeRuleResponseVM fromEntity(DentalChartAgeRule entity) {
        return new DentalChartAgeRuleResponseVM(
                entity.getId(),
                entity.getName(),
                entity.getAgeFrom(),
                entity.getAgeTo(),
                entity.getChartType(),
                entity.getNumberingSystem() != null
                        ? entity.getNumberingSystem()
                        : ToothNumberingSystem.UNIVERSAL,
                entity.getToothNumberList(),
                entity.getIsActive()
        );
    }
}
