package com.dazzle.asklepios.web.rest.vm.nphiespayer;

import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.TpaDefinition;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.Hibernate;

import java.time.Instant;
import java.util.List;

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
        List<Long> tpaIds,
        List<LinkedTpaVM> tpas,
        Instant createdDate,
        Instant lastModifiedDate
) {
    public static NphiesPayerResponseVM ofEntity(NphiesPayer payer) {
        boolean tpasLoaded = payer.getTpas() != null && Hibernate.isInitialized(payer.getTpas());
        List<LinkedTpaVM> tpas = tpasLoaded
                ? payer.getTpas().stream().map(LinkedTpaVM::ofEntity).toList()
                : List.of();

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
                tpas.stream().map(LinkedTpaVM::id).toList(),
                tpas,
                payer.getCreatedDate(),
                payer.getLastModifiedDate()
        );
    }

    public record LinkedTpaVM(
            @JsonProperty("id") Long id,
            @JsonProperty("tpaId") Long tpaId,
            @JsonProperty("tpaCode") String tpaCode,
            @JsonProperty("name") String name,
            @JsonProperty("isActive") Boolean isActive
    ) {
        public static LinkedTpaVM ofEntity(TpaDefinition tpa) {
            Long tpaId = tpa.getId();
            return new LinkedTpaVM(
                    tpaId,
                    tpaId,
                    tpa.getTpaCode(),
                    tpa.getName(),
                    tpa.getIsActive()
            );
        }
    }
}
