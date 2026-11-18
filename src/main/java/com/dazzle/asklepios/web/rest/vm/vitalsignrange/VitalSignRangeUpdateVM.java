package com.dazzle.asklepios.web.rest.vm.vitalsignrange;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Age bounds are whole months: [ageFromMonths, ageToMonths). A null ageToMonths means no upper limit.
 * Both thresholds are optional (null = not configured); non-numeric JSON values are rejected with 400.
 */
public record VitalSignRangeUpdateVM(
        @NotNull @Min(0) Integer ageFromMonths,
        @Min(1) Integer ageToMonths,
        BigDecimal normalMin,
        BigDecimal normalMax
) {
}
