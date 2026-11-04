package com.dazzle.asklepios.web.rest.vm.pricelist;

import com.dazzle.asklepios.domain.PriceListItem;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record PriceListItemResponseVM(

        Long id,
        Long priceListId,
        BillingItemTypes itemType,

        // polymorphic ids
        Long serviceId,
        Long brandMedicationId,
        Long diagnosticTestId,
        Long procedureId,

        BigDecimal price,
        Boolean discountAllowed,
        Boolean isActive,

        Instant createdDate,
        Instant lastModifiedDate

) implements Serializable {

    public static PriceListItemResponseVM ofEntity(PriceListItem p) {

        return new PriceListItemResponseVM(
                p.getId(),
                p.getPriceListId(),
                p.getItemType(),

                // relations (safe null handling)
                p.getService() != null ? p.getService().getId() : null,
                p.getBrandMedication() != null ? p.getBrandMedication().getId() : null,
                p.getDiagnosticTest() != null ? p.getDiagnosticTest().getId() : null,
                p.getProcedure() != null ? p.getProcedure().getId() : null,

                p.getPrice(),
                p.getDiscountAllowed(),
                p.getIsActive(),

                p.getCreatedDate(),
                p.getLastModifiedDate()
        );
    }
}