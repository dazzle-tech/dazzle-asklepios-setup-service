package com.dazzle.asklepios.web.rest.vm.nphiespayer;

import com.dazzle.asklepios.domain.enumeration.ApprovalCoverageCompany;
import com.dazzle.asklepios.web.rest.vm.jackson.FlexibleApprovalCoverageCompanyDeserializer;
import com.dazzle.asklepios.web.rest.vm.jackson.FlexibleLongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;

public record NphiesPayerUpdateVM(

        @NotNull(message = "id cannot be null")
        @JsonDeserialize(using = FlexibleLongDeserializer.class)
        Long id,

        @NotBlank(message = "nphiesId cannot be blank")
        @Size(max = 100)
        String nphiesId,

        @NotBlank(message = "nameEn cannot be blank")
        @Size(max = 255)
        String nameEn,

        @Size(max = 255)
        String nameAr,

        @Size(max = 100)
        String shortName,

        @NotNull(message = "facilityId cannot be null")
        @JsonDeserialize(using = FlexibleLongDeserializer.class)
        Long facilityId,

        @NotBlank(message = "insuranceAuthorityLicenseNo cannot be blank")
        @Size(max = 100)
        String insuranceAuthorityLicenseNo,

        @NotBlank(message = "commercialRegistrationNo cannot be blank")
        @Size(max = 100)
        String commercialRegistrationNo,

        @NotBlank(message = "vatRegistrationNo cannot be blank")
        @Size(max = 100)
        String vatRegistrationNo,

        @Size(max = 100)
        String unifiedNationalNo,

        @Size(max = 2000)
        String headOfficeAddress,

        @JsonDeserialize(using = FlexibleLongDeserializer.class)
        Long countryId,

        @JsonDeserialize(using = FlexibleLongDeserializer.class)
        Long cityId,

        @Size(max = 20)
        String postalCode,

        @Size(max = 255)
        String contactPerson,

        @NotBlank(message = "phone cannot be blank")
        @Size(max = 50)
        String phone,

        @Size(max = 50)
        String mobile,

        @NotBlank(message = "email cannot be blank")
        @Email
        @Size(max = 255)
        String email,

        @Size(max = 255)
        String website,

        Boolean isActive,

        @JsonDeserialize(using = FlexibleApprovalCoverageCompanyDeserializer.class)
        ApprovalCoverageCompany approvalCoverageCompany,

        @JsonDeserialize(contentUsing = FlexibleLongDeserializer.class)
        List<Long> tpaIds

) implements Serializable {}
