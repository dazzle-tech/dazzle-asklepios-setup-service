package com.dazzle.asklepios.web.rest.vm.dentalchartagerule;

import com.dazzle.asklepios.domain.DentalChartAgeRule;
import com.dazzle.asklepios.domain.enumeration.DentalChartType;
import com.dazzle.asklepios.domain.enumeration.ToothNumberingSystem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DentalChartAgeRuleUpdateVM(
        @NotNull Long id,
        @NotBlank String name,
        @NotNull @Min(0) Integer ageFrom,
        @Min(0) Integer ageTo,
        @NotNull DentalChartType chartType,
        @NotNull ToothNumberingSystem numberingSystem,
        List<String> toothNumbers,
        @NotNull Boolean isActive
) {
    public DentalChartAgeRule toEntity() {
        DentalChartAgeRule entity = DentalChartAgeRule.builder()
                .id(id)
                .name(name != null ? name.trim() : null)
                .ageFrom(ageFrom)
                .ageTo(ageTo)
                .chartType(chartType)
                .numberingSystem(numberingSystem)
                .isActive(isActive)
                .build();
        entity.setToothNumberList(toothNumbers);
        return entity;
    }
}
