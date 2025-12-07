package com.dazzle.asklepios.web.rest.vm.pricelist;

import com.dazzle.asklepios.domain.PriceList;
import com.dazzle.asklepios.domain.enumeration.biling.PriceListTypes;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

public record PriceListResponseVM(
        Long id,
        Long facilityId,
        String name,
        PriceListTypes type,
        String currency,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String description,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {
    public static PriceListResponseVM ofEntity(PriceList p) {
        return new PriceListResponseVM(
                p.getId(),
                p.getFacilityId(),
                p.getName(),
                p.getType(),
                p.getCurrency().name(),
                p.getEffectiveFrom(),
                p.getEffectiveTo(),
                p.getDescription(),
                p.getIsActive(),
                p.getCreatedDate(),
                p.getLastModifiedDate()
        );
    }
}


