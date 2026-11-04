package com.dazzle.asklepios.service.dto.medFrequencySetup;

import com.dazzle.asklepios.domain.enumeration.FrequencyList;
import com.dazzle.asklepios.domain.enumeration.MedFrequency;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Create body: e.g. EVERY_2_HOURS -> frequencyNumber = 2, frequencyUnit = HOURS */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MedFrequencySetupCreateDTO(

        @NotNull
        MedFrequency frequency,

        @NotNull
        @Min(1)
        Integer frequencyNumber,

        @NotNull
        FrequencyList frequencyUnit
) {
}
