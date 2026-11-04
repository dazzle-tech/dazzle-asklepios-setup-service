package com.dazzle.asklepios.web.rest.vm.surveyText;

import com.dazzle.asklepios.domain.SurveyText;

import java.time.Instant;

public record SurveyTextResponseVM(
        Long id,
        String textKey,
        String textEn,
        String textAr,
        Boolean isActive,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate
) {
    public static SurveyTextResponseVM ofEntity(SurveyText entity) {
        if (entity == null) return null;
        return new SurveyTextResponseVM(
                entity.getId(),
                entity.getTextKey(),
                entity.getTextEn(),
                entity.getTextAr(),
                entity.getIsActive(),
                entity.getCreatedBy(),
                entity.getCreatedDate(),
                entity.getLastModifiedBy(),
                entity.getLastModifiedDate()
        );
    }
}
