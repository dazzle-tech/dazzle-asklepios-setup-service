package com.dazzle.asklepios.web.rest.vm.normalrange;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestNormalRange;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.enumeration.AgeUnit;
import com.dazzle.asklepios.domain.enumeration.Condition;
import com.dazzle.asklepios.domain.enumeration.NormalRangeType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record DiagnosticTestNormalRangeUpdateVM(
        @NotNull Long id,
        Long testId,
        String gender,
        Double ageFrom,
        AgeUnit ageFromUnit,
        Double ageTo,
        AgeUnit ageToUnit,
        Condition condition,
        String resultText,
        String resultLov,
        NormalRangeType normalRangeType,
        Double rangeFrom,
        Double rangeTo,
        Boolean criticalValue,
        Double criticalValueLessThan,
        Double criticalValueMoreThan,
        @NotNull Long profileTestId,
        List<String> lovKeys
) {
    public DiagnosticTestNormalRange toEntity() {
        return DiagnosticTestNormalRange.builder()
                .id(id)
                .test(testId != null ? DiagnosticTest.builder().id(testId).build() : null)
                .gender(gender)
                .ageFrom(ageFrom)
                .ageFromUnit(ageFromUnit)
                .ageTo(ageTo)
                .ageToUnit(ageToUnit)
                .condition(condition)
                .resultText(resultText)
                .resultLov(resultLov)
                .normalRangeType(normalRangeType)
                .rangeFrom(rangeFrom)
                .rangeTo(rangeTo)
                .criticalValue(criticalValue)
                .criticalValueLessThan(criticalValueLessThan)
                .criticalValueMoreThan(criticalValueMoreThan)
                .profileTest(DiagnosticTestProfile.builder().id(profileTestId).build())
                .lovKeys(lovKeys)
                .build();
    }
}
