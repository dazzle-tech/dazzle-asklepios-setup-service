package com.dazzle.asklepios.service.dto.surveyText;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Create body: the key is how the survey page finds the text, e.g. SURVEY_TITLE */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SurveyTextCreateDTO(

        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$", message = "Key can only have letters, numbers and _")
        String textKey,

        @NotBlank
        String textEn,

        @NotBlank
        String textAr
) {
}
