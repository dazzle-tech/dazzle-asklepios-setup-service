package com.dazzle.asklepios.web.rest.vm.tpadefinition;

import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record TpaDefinitionUpdateVM(

        @NotNull(message = "id cannot be null")
        Long id,

        @NotBlank(message = "tpaCode cannot be blank")
        @Size(max = 100)
        String tpaCode,

        @NotBlank(message = "name cannot be blank")
        @Size(max = 255)
        String name,

        GuarantorType guarantorType,

        @NotNull(message = "activationDate cannot be null")
        LocalDate activationDate,

        Boolean isActive,

        @Size(max = 100)
        String taxRegistrationNo,

        Long countryId,

        Long cityId,

        @Size(max = 2000)
        String address,

        @Size(max = 50)
        String phone,

        @Email
        @Size(max = 255)
        String email,

        List<Long> insuranceCompanyIds

) implements Serializable {}
