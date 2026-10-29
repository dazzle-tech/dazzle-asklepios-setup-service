package com.dazzle.asklepios.service.dto.surveyQuestion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Update body: only the order and whether the question is required can change */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SurveyQuestionUpdateDTO(

        @NotNull
        @Min(1)
        Integer displayOrder,

        @NotNull
        Boolean isRequired
) {
}
