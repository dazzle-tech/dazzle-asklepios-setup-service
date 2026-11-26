package com.dazzle.asklepios.service.dto.surveyQuestion;

import com.dazzle.asklepios.domain.enumeration.SurveyAnswerType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Create body. SCALE needs minValue/maxValue (the screen suggests 1 to 5), TEXT has no limits.
 * After it is created, only the order and isRequired change (see SurveyQuestionUpdateDTO).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SurveyQuestionCreateDTO(

        @NotNull
        Long sectionId,

        @NotNull
        @Min(1)
        Integer displayOrder,

        @NotBlank
        @Size(max = 1000)
        String questionEn,

        @NotBlank
        @Size(max = 1000)
        String questionAr,

        @NotNull
        SurveyAnswerType answerType,

        Integer minValue,

        Integer maxValue,

        /** Defaults to false when not sent */
        Boolean isRequired
) {
}
