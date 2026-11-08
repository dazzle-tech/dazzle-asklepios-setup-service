package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;

import java.time.LocalDate;

public record PriceListSetupCloneRequest(

        Boolean cloneItems,

        Long facilityId,

        Boolean appliesToAllFacilities,

        PriceListSetupType type,

        Long payerId,

        Long nphiesPayerId,

        String name,

        String shortName,

        String description,

        Integer versionNumber,

        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        Currency currency,

        PriceListSetupStatus status,

        Long taxId

) {
}
