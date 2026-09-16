package com.dazzle.asklepios.web.rest.vm.tpadefinition;

import com.dazzle.asklepios.domain.enumeration.ApprovalCoverageCompany;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import com.dazzle.asklepios.web.rest.vm.jackson.FlexibleLongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record TpaDefinitionUpdateVM(

        @NotNull(message = "id cannot be null")
        @JsonDeserialize(using = FlexibleLongDeserializer.class)
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

        @JsonDeserialize(using = FlexibleLongDeserializer.class)
        Long countryId,

        @JsonDeserialize(using = FlexibleLongDeserializer.class)
        Long cityId,

        @Size(max = 2000)
        String address,

        @Size(max = 50)
        String phone,

        @Email
        @Size(max = 255)
        String email,

        ApprovalCoverageCompany approvalCoverageCompany,

        @JsonDeserialize(contentUsing = FlexibleLongDeserializer.class)
        List<Long> insuranceCompanyIds

) implements Serializable {}
