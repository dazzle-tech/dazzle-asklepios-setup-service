package com.dazzle.asklepios.web.rest.vm.dentalchartagerule;

import com.dazzle.asklepios.domain.DentalChartAgeRule;
import com.dazzle.asklepios.domain.enumeration.DentalChartType;
import com.dazzle.asklepios.domain.enumeration.ToothNumberingSystem;

import java.util.List;
import java.util.Map;

public record DentalChartAgeRuleResponseVM(
        Long id,
        String name,
        Integer ageFrom,
        Integer ageTo,
        DentalChartType chartType,
        ToothNumberingSystem numberingSystem,
        Long numberingMethodId,
        Map<String, String> numberingLabels,
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
                entity.getNumberingSystem(),
                entity.getNumberingMethodId(),
                null,
                entity.getToothNumberList(),
                entity.getIsActive()
        );
    }

    /** Copy carrying the resolved method labels (set only by the resolve endpoint). */
    public DentalChartAgeRuleResponseVM withNumberingLabels(Map<String, String> labels) {
        return new DentalChartAgeRuleResponseVM(
                id, name, ageFrom, ageTo, chartType, numberingSystem,
                numberingMethodId, labels, toothNumbers, isActive);
    }
}
