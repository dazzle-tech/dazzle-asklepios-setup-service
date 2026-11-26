package com.dazzle.asklepios.web.rest.vm.insurancecompanypolicy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;

public record InsuranceCompanyPolicySaveVM(

        @NotBlank(message = "policyName cannot be blank")
        @Size(max = 255)
        String policyName,

        @Size(max = 100)
        String policyNumber,

        @NotBlank(message = "policyCode cannot be blank")
        @Size(max = 100)
        String policyCode,

        @NotNull(message = "validFrom cannot be null")
        LocalDate validFrom,

        LocalDate validTo,

        Boolean isActive

) implements Serializable {

    public InsuranceCompanyPolicySaveVM(
            String policyName,
            String policyNumber,
            String policyCode,
            LocalDate validFrom,
            LocalDate validTo
    ) {
        this(policyName, policyNumber, policyCode, validFrom, validTo, null);
    }
}
