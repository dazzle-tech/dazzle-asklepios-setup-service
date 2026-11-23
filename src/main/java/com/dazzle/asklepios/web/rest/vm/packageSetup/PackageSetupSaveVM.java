package com.dazzle.asklepios.web.rest.vm.packageSetup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** The whole package (header and items) is saved in one request; items replace the stored ones. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PackageSetupSaveVM(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 1000) String description,
        @DecimalMin("0.0000") BigDecimal price,
        Boolean isActive,
        @NotEmpty List<@Valid PackageSetupItemVM> items
) implements Serializable {}
