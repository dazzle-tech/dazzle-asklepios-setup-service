package com.dazzle.asklepios.web.rest.vm.nphiespayer;

import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.domain.enumeration.ApprovalCoverageCompany;
import org.hibernate.Hibernate;

import java.time.Instant;
import java.util.Comparator;
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
        ApprovalCoverageCompany approvalCoverageCompany,
        List<Long> tpaIds,
        List<LinkedTpaVM> tpas,
        List<Long> childCompanyIds,
        List<LinkedInsuranceVM> childCompanies,
        LinkedInsuranceVM parentCompany,
        Instant createdDate,
        Instant lastModifiedDate
) {
    public static NphiesPayerResponseVM ofEntity(NphiesPayer payer) {
        boolean tpasLoaded = payer.getTpas() != null && Hibernate.isInitialized(payer.getTpas());
        List<LinkedTpaVM> tpas = tpasLoaded
                ? payer.getTpas().stream().map(LinkedTpaVM::ofEntity).toList()
                : List.of();
        boolean childrenLoaded = payer.getChildCompanies() != null
                && Hibernate.isInitialized(payer.getChildCompanies());
        List<LinkedInsuranceVM> childCompanies = childrenLoaded
                ? payer.getChildCompanies().stream()
                        .sorted(Comparator.comparing(NphiesPayer::getNameEn, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                        .map(LinkedInsuranceVM::ofEntity)
                        .toList()
                : List.of();
        boolean parentsLoaded = payer.getParentCompanies() != null
                && Hibernate.isInitialized(payer.getParentCompanies());
        LinkedInsuranceVM parentCompany = parentsLoaded
                ? payer.getParentCompanies().stream().findFirst().map(LinkedInsuranceVM::ofEntity).orElse(null)
                : null;

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
                payer.getApprovalCoverageCompany(),
                tpas.stream().map(LinkedTpaVM::id).toList(),
                tpas,
                childCompanies.stream().map(LinkedInsuranceVM::id).toList(),
                childCompanies,
                parentCompany,
                payer.getCreatedDate(),
                payer.getLastModifiedDate()
        );
    }

    public record LinkedTpaVM(
            Long id,
            String tpaCode,
            String name,
            Boolean isActive
    ) {
        public static LinkedTpaVM ofEntity(TpaDefinition tpa) {
            return new LinkedTpaVM(
                    tpa.getId(),
                    tpa.getTpaCode(),
                    tpa.getName(),
                    tpa.getIsActive()
            );
        }
    }

    public record LinkedInsuranceVM(
            Long id,
            String nphiesId,
            String nameEn,
            String nameAr,
            Boolean isActive
    ) {
        public static LinkedInsuranceVM ofEntity(NphiesPayer payer) {
            return new LinkedInsuranceVM(
                    payer.getId(),
                    payer.getNphiesId(),
                    payer.getNameEn(),
                    payer.getNameAr(),
                    payer.getIsActive()
            );
        }
    }
}
