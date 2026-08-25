package com.dazzle.asklepios.web.rest.vm.nphiespayer;

import com.dazzle.asklepios.domain.NphiesPayer;

import java.time.Instant;

public record NphiesPayerResponseVM(
        Long id,
        String nphiesId,
        String nameEn,
        String nameAr,
        String shortName,
        Long facilityId,
        String facilityName,
        String insuranceAuthorityLicenseNo,
        String commercialRegistrationNo,
        String vatRegistrationNo,
        String unifiedNationalNo,
        String headOfficeAddress,
        Long countryId,
        String countryName,
        Long cityId,
        String cityName,
        String postalCode,
        String contactPerson,
        String phone,
        String mobile,
        String email,
        String website,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {
    public static NphiesPayerResponseVM ofEntity(NphiesPayer payer) {
        return new NphiesPayerResponseVM(
                payer.getId(),
                payer.getNphiesId(),
                payer.getNameEn(),
                payer.getNameAr(),
                payer.getShortName(),
                payer.getFacility() != null ? payer.getFacility().getId() : null,
                payer.getFacility() != null ? payer.getFacility().getName() : null,
                payer.getInsuranceAuthorityLicenseNo(),
                payer.getCommercialRegistrationNo(),
                payer.getVatRegistrationNo(),
                payer.getUnifiedNationalNo(),
                payer.getHeadOfficeAddress(),
                payer.getCountry() != null ? payer.getCountry().getId() : null,
                payer.getCountry() != null && payer.getCountry().getName() != null
                        ? payer.getCountry().getName().name()
                        : null,
                payer.getCity() != null ? payer.getCity().getId() : null,
                payer.getCity() != null ? payer.getCity().getName() : null,
                payer.getPostalCode(),
                payer.getContactPerson(),
                payer.getPhone(),
                payer.getMobile(),
                payer.getEmail(),
                payer.getWebsite(),
                payer.getIsActive(),
                payer.getCreatedDate(),
                payer.getLastModifiedDate()
        );
    }
}
