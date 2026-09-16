package com.dazzle.asklepios.web.rest.vm.tpadefinition;

import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.domain.enumeration.ApprovalCoverageCompany;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import org.hibernate.Hibernate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record TpaDefinitionResponseVM(
        Long id,
        String tpaCode,
        String name,
        GuarantorType guarantorType,
        LocalDate activationDate,
        Boolean isActive,
        ApprovalCoverageCompany approvalCoverageCompany,
        String taxRegistrationNo,
        Long countryId,
        String countryName,
        Long cityId,
        String cityName,
        String address,
        String phone,
        String email,
        List<Long> insuranceCompanyIds,
        List<TpaLinkedInsuranceCompanyVM> insuranceCompanies,
        long linkedInsuranceCount,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate
) {
    public static TpaDefinitionResponseVM ofEntity(TpaDefinition tpa) {
        return ofEntity(tpa, null);
    }

    public static TpaDefinitionResponseVM ofEntity(TpaDefinition tpa, Long linkedInsuranceCount) {
        boolean companiesLoaded = tpa.getInsuranceCompanies() != null
                && Hibernate.isInitialized(tpa.getInsuranceCompanies());
        List<TpaLinkedInsuranceCompanyVM> companies = companiesLoaded
                ? tpa.getInsuranceCompanies().stream()
                        .map(TpaLinkedInsuranceCompanyVM::ofEntity)
                        .toList()
                : List.of();
        long count = linkedInsuranceCount != null ? linkedInsuranceCount : companies.size();

        return new TpaDefinitionResponseVM(
                tpa.getId(),
                tpa.getTpaCode(),
                tpa.getName(),
                tpa.getGuarantorType(),
                tpa.getActivationDate(),
                tpa.getIsActive(),
                tpa.getApprovalCoverageCompany(),
                tpa.getTaxRegistrationNo(),
                tpa.getCountry() != null ? tpa.getCountry().getId() : null,
                tpa.getCountry() != null && tpa.getCountry().getName() != null
                        ? tpa.getCountry().getName().name()
                        : null,
                tpa.getCity() != null ? tpa.getCity().getId() : null,
                tpa.getCity() != null ? tpa.getCity().getName() : null,
                tpa.getAddress(),
                tpa.getPhone(),
                tpa.getEmail(),
                companies.stream().map(TpaLinkedInsuranceCompanyVM::id).toList(),
                companies,
                count,
                tpa.getCreatedBy(),
                tpa.getCreatedDate(),
                tpa.getLastModifiedBy(),
                tpa.getLastModifiedDate()
        );
    }

    public record TpaLinkedInsuranceCompanyVM(
            Long id,
            String nphiesId,
            String nameEn,
            String nameAr,
            Boolean isActive
    ) {
        public static TpaLinkedInsuranceCompanyVM ofEntity(NphiesPayer payer) {
            return new TpaLinkedInsuranceCompanyVM(
                    payer.getId(),
                    payer.getNphiesId(),
                    payer.getNameEn(),
                    payer.getNameAr(),
                    payer.getIsActive()
            );
        }
    }
}
