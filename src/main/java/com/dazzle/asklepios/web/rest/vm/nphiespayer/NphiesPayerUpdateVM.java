package com.dazzle.asklepios.web.rest.vm.nphiespayer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;

public record NphiesPayerUpdateVM(

        @NotNull(message = "id cannot be null")
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

        Long countryId,

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

        List<Long> tpaIds

) implements Serializable {}
