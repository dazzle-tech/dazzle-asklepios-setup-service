package com.dazzle.asklepios.web.rest.vm.surveySection;

import com.dazzle.asklepios.domain.SurveySection;

import java.time.Instant;

public record SurveySectionResponseVM(
        Long id,
        String nameEn,
        String nameAr,
        Integer displayOrder,
        Boolean isActive,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate
) {
    public static SurveySectionResponseVM ofEntity(SurveySection entity) {
        if (entity == null) return null;
        return new SurveySectionResponseVM(
                entity.getId(),
                entity.getNameEn(),
                entity.getNameAr(),
                entity.getDisplayOrder(),
                entity.getIsActive(),
                entity.getCreatedBy(),
                entity.getCreatedDate(),
                entity.getLastModifiedBy(),
                entity.getLastModifiedDate()
        );
    }
}
