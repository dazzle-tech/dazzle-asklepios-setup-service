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

        @NotBlank
        @Size(max = 50)
        String code,

        @NotBlank
        @Size(max = 255)
        String name,

        @NotNull
        PayorCategory category,

        @Size(max = 500)
        String address,

        @Size(max = 50)
        String phone,

        @Email
        @Size(max = 255)
        String email,

        @Size(max = 255)
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

        @Size(max = 100)
        String nphiesId,

        @Size(max = 100)
        String waseelPayerId,

        @Size(max = 100)
        String tpaNphiesId,

        Boolean isWaseelEnabled,

        Boolean isActive

) implements Serializable {}