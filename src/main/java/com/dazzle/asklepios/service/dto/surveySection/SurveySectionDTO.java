package com.dazzle.asklepios.service.dto.surveySection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Create and update body of a survey section */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SurveySectionDTO(

        @NotBlank
        @Size(max = 255)
        String nameEn,

        @NotBlank
        @Size(max = 255)
        String nameAr,

        @NotNull
        @Min(1)
        Integer displayOrder
) {
}
