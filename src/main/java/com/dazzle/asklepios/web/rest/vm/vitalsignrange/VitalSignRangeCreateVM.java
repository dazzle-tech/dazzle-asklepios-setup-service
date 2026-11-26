package com.dazzle.asklepios.web.rest.vm.vitalsignrange;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VitalSignRangeCreateVM(
        @NotBlank String vitalSignKey,
        @NotNull @Min(0) Integer ageFromMonths,
        @Min(1) Integer ageToMonths,
        BigDecimal normalMin,
        BigDecimal normalMax
) {
}
