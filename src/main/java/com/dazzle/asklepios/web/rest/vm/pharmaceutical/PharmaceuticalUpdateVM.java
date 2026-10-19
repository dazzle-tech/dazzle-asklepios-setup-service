package com.dazzle.asklepios.web.rest.vm.pharmaceutical;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PharmaceuticalUpdateVM(
        @NotNull Long id,
        @NotEmpty @Size(max = 255) String pharmaceuticalName,
        @Size(max = 50) String code,
        @Size(max = 50) String billingCode,
        @Size(max = 255) String itemDescription,
        @Size(max = 255) String secondDescription,
        Boolean isActive
) implements Serializable {}
