package com.dazzle.asklepios.web.rest.vm.nphiespayer;

import java.time.LocalDate;
import java.util.List;

public record PayerRelationshipDashboardVM(
        SummaryVM summary,
        List<InsuranceListItemVM> insurances,
        List<TpaListItemVM> tpas
) {
    public record SummaryVM(
            int insuranceCount,
            int tpaCount,
            int parentCompanyCount,
            int childCompanyCount,
            int insurancesWithContracts,
            int insurancesWithPriceLists,
            int tpasWithContracts
    ) {}

    public record InsuranceListItemVM(
            Long id,
            String nphiesId,
            String nameEn,
            String nameAr,
            Boolean isActive,
            String role
    ) {}

    public record TpaListItemVM(
            Long id,
            String tpaCode,
            String name,
            Boolean isActive
    ) {}

    public record LinkedPartyVM(
            Long id,
            String code,
            String name,
            Boolean isActive
    ) {}

    public record PriceListItemVM(
            Long id,
            String name,
            String status,
            String type,
            Boolean isActive,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {}

    public record ContractItemVM(
            Long id,
            String code,
            String policyNumber,
            String guarantorType,
            String className,
            Boolean isActive,
            Long insurancePayerId,
            String insurancePayerName,
            Long priceListSetupId,
            String priceListName
    ) {}

    public record InsuranceCardVM(
            Long id,
            String nphiesId,
            String nameEn,
            String nameAr,
            Boolean isActive,
            String approvalCoverageCompany,
            String facilityName,
            String role,
            LinkedPartyVM parent,
            List<LinkedPartyVM> childCompanies,
            List<LinkedPartyVM> tpas,
            List<PriceListItemVM> priceLists,
            List<ContractItemVM> contracts
    ) {}

    public record TpaCardVM(
            Long id,
            String tpaCode,
            String name,
            Boolean isActive,
            String approvalCoverageCompany,
            List<LinkedPartyVM> insuranceCompanies,
            List<ContractItemVM> contracts
    ) {}
}
