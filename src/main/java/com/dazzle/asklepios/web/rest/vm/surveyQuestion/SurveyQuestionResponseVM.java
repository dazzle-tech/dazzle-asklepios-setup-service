package com.dazzle.asklepios.web.rest.vm.surveyQuestion;

import com.dazzle.asklepios.domain.SurveyQuestion;
import com.dazzle.asklepios.domain.enumeration.SurveyAnswerType;

import java.time.Instant;

public record SurveyQuestionResponseVM(
        Long id,
        Long sectionId,
        Integer displayOrder,
        String questionEn,
        String questionAr,
        SurveyAnswerType answerType,
        Integer minValue,
        Integer maxValue,
        Boolean isRequired,
        Boolean isActive,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate
) {
    public static SurveyQuestionResponseVM ofEntity(SurveyQuestion entity) {
        if (entity == null) return null;
        return new SurveyQuestionResponseVM(
                entity.getId(),
                entity.getSectionId(),
                entity.getDisplayOrder(),
                entity.getQuestionEn(),
                entity.getQuestionAr(),
                entity.getAnswerType(),
                entity.getMinValue(),
                entity.getMaxValue(),
                entity.getIsRequired(),
                entity.getIsActive(),
                entity.getCreatedBy(),
                entity.getCreatedDate(),
                entity.getLastModifiedBy(),
                entity.getLastModifiedDate()
        );
    }
}
