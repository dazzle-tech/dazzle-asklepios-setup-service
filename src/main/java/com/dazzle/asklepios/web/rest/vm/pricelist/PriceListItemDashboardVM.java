package com.dazzle.asklepios.web.rest.vm.pricelist;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PriceListItemDashboardVM(
        SummaryVM summary,
        List<PriceListColumnVM> priceLists
) {
    public record SummaryVM(
            int priceListCount,
            int uniqueItemCount,
            int completeItemCount,
            int partialItemCount,
            List<TypeCountVM> byItemType
    ) {}

    public record TypeCountVM(
            PriceListItemType itemType,
            int itemCount
    ) {}

    public record ItemPageVM(
            List<CatalogItemVM> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    public record CatalogItemVM(
            PriceListItemType itemType,
            Long sourceId,
            String itemCode,
            String itemName,
            String category,
            String nonStandardCode,
            int presentInCount,
            int missingFromCount,
            String coverageStatus,
            int coveragePercent,
            BigDecimal minUnitPrice,
            BigDecimal maxUnitPrice,
            boolean hasPriceVariance
    ) {}

    public record PriceListColumnVM(
            Long id,
            String name,
            String shortName,
            String description,
            PriceListSetupType type,
            PriceListSetupStatus status,
            Boolean isActive,
            Currency currency,
            Integer versionNumber,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Long facilityId,
            String facilityName,
            Boolean appliesToAllFacilities,
            Long payerId,
            String payerName,
            Long nphiesPayerId,
            String nphiesPayerName
    ) {}

    public record ItemCardVM(
            PriceListItemType itemType,
            Long sourceId,
            String itemCode,
            String itemName,
            String category,
            String nonStandardCode,
            int presentInCount,
            int missingFromCount,
            String coverageStatus,
            int coveragePercent,
            boolean hasPriceVariance,
            BigDecimal minUnitPrice,
            BigDecimal maxUnitPrice,
            List<PriceListCoverageVM> priceLists
    ) {}

    public record PriceListCoverageVM(
            PriceListColumnVM priceList,
            boolean present,
            String presence,
            List<ItemEntryVM> entries
    ) {}

    public record ItemEntryVM(
            Long id,
            Long priceListSetupId,
            Long waseelItemMappingId,
            Long sbsCatalogId,
            String itemCode,
            String nonStandardCode,
            String itemName,
            String category,
            EncounterType visitType,
            BigDecimal unitPrice,
            BigDecimal cost,
            BigDecimal discountPercentage,
            BigDecimal netPrice,
            Boolean isActive,
            Boolean requiresPreAuthorization,
            Boolean visitTypeLocked,
            Instant createdDate,
            Instant lastModifiedDate
    ) {}
}
