package com.dazzle.asklepios.service.dto.mobileAppConfig;

import com.dazzle.asklepios.domain.enumeration.MobileAppConfigKey;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MobileAppConfigCreateDTO(
        @NotNull MobileAppConfigKey configKey,
        @NotNull MobileAppConfigType configType,
        @NotBlank @Size(max = 4000) String configValue
) {
}
