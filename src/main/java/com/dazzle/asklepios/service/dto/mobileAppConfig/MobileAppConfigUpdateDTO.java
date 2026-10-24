package com.dazzle.asklepios.service.dto.mobileAppConfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MobileAppConfigUpdateDTO(
        @NotBlank @Size(max = 4000) String configValue
) {
}
