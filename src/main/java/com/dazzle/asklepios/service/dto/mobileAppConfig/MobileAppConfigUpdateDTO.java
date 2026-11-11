package com.dazzle.asklepios.service.dto.mobileAppConfig;

import com.dazzle.asklepios.domain.enumeration.MobileAppConfigType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MobileAppConfigUpdateDTO(
        @NotNull MobileAppConfigType configType,
        @NotEmpty List<String> configValues,
        @Size(max = 1000) String description
) {
}
