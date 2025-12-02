package com.dazzle.asklepios.web.rest.vm.payor;

import com.dazzle.asklepios.domain.enumeration.biling.PayorCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;

public record PayorUpdateVM(

        @NotNull(message = "id cannot be null")
        Long id,

        @NotBlank @Size(max=50)
        String code,

        @NotBlank @Size(max=255)
        String name,

        @NotNull
        PayorCategory category,

        String address,
        String phone,
        @Email String email,
        String contractManagerContact,

        LocalDate startDate,
        LocalDate expiryDate,
        Boolean renewable,

        Boolean allowPartialCoverage,
        Boolean acceptCopay,
        Boolean acceptDeductibles,
        Boolean allowPackagePricing,
        Boolean allowDrgBilling,
        Boolean forcePreApproval,

        Boolean isActive

) implements Serializable {}
