package com.dazzle.asklepios.web.rest.vm.normalrange;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestNormalRange;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.enumeration.AgeUnit;
import com.dazzle.asklepios.domain.enumeration.Condition;
import com.dazzle.asklepios.domain.enumeration.NormalRangeType;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record DiagnosticTestNormalRangeUpdateVM(
        @NotNull Long id,
        @NotNull Long testId,
        String gender,
        Double ageFrom,
        AgeUnit ageFromUnit,
        Double ageTo,
        AgeUnit ageToUnit,
        Condition condition,
        @NotNull TestResultType resultType,
        String resultText,
        String resultLov,
        NormalRangeType normalRangeType,
        Double rangeFrom,
        Double rangeTo,
        Boolean criticalValue,
        Double criticalValueLessThan,
        Double criticalValueMoreThan,
        Long profileTestId,
        Boolean isProfile,
        List<String> lovKeys
) {
    public DiagnosticTestNormalRange toEntity() {
        return DiagnosticTestNormalRange.builder()
                .id(id)
                .test(DiagnosticTest.builder().id(testId).build())
                .gender(gender)
                .ageFrom(ageFrom)
                .ageFromUnit(ageFromUnit)
                .ageTo(ageTo)
                .ageToUnit(ageToUnit)
                .condition(condition)
                .resultType(resultType)
                .resultText(resultText)
                .resultLov(resultLov)
                .normalRangeType(normalRangeType)
                .rangeFrom(rangeFrom)
                .rangeTo(rangeTo)
                .criticalValue(criticalValue)
                .criticalValueLessThan(criticalValueLessThan)
                .criticalValueMoreThan(criticalValueMoreThan)
                .profileTest(profileTestId != null ? DiagnosticTestProfile.builder().id(profileTestId).build() : null)
                .isProfile(isProfile)
                .lovKeys(lovKeys)
                .build();
    }
}
