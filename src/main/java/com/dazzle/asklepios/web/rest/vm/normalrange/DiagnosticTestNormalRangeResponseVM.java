package com.dazzle.asklepios.web.rest.vm.normalrange;

import com.dazzle.asklepios.domain.DiagnosticTestNormalRange;
import com.dazzle.asklepios.domain.enumeration.AgeUnit;
import com.dazzle.asklepios.domain.enumeration.Condition;
import com.dazzle.asklepios.domain.enumeration.NormalRangeType;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import lombok.Builder;

import java.util.List;

@Builder
public record DiagnosticTestNormalRangeResponseVM(
        Long id,
        Long testId,
        String gender,
        Double ageFrom,
        AgeUnit ageFromUnit,
        Double ageTo,
        AgeUnit ageToUnit,
        Condition condition,
        TestResultType resultType,
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
    public static DiagnosticTestNormalRangeResponseVM fromEntity(DiagnosticTestNormalRange e) {
        return DiagnosticTestNormalRangeResponseVM.builder()
                .id(e.getId())
                .testId(e.getTest() != null ? e.getTest().getId() : null)
                .gender(e.getGender())
                .ageFrom(e.getAgeFrom())
                .ageFromUnit(e.getAgeFromUnit())
                .ageTo(e.getAgeTo())
                .ageToUnit(e.getAgeToUnit())
                .condition(e.getCondition())
                .resultType(e.getResultType())
                .resultText(e.getResultText())
                .resultLov(e.getResultLov())
                .normalRangeType(e.getNormalRangeType())
                .rangeFrom(e.getRangeFrom())
                .rangeTo(e.getRangeTo())
                .criticalValue(e.getCriticalValue())
                .criticalValueLessThan(e.getCriticalValueLessThan())
                .criticalValueMoreThan(e.getCriticalValueMoreThan())
                .profileTestId(e.getProfileTest() != null ? e.getProfileTest().getId() : null)
                .isProfile(e.getIsProfile())
                .lovKeys(e.getLovKeys())
                .build();
    }
}
