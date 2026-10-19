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

        @NotBlank(message = "policyNumber cannot be blank")
        @Size(max = 100)
        String policyNumber,

        @NotBlank(message = "policyCode cannot be blank")
        @Size(max = 100)
        String policyCode,

        @NotNull(message = "validFrom cannot be null")
        LocalDate validFrom,

        @NotNull(message = "validTo cannot be null")
        LocalDate validTo

) implements Serializable {}
