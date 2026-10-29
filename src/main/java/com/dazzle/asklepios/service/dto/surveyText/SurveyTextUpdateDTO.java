package com.dazzle.asklepios.service.dto.surveyText;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

/** Update body: only the texts change, the key stays because the survey page uses it */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SurveyTextUpdateDTO(

        @NotBlank
        String textEn,

        @NotBlank
        String textAr
) {
}
