package com.dazzle.asklepios.web.rest.vm.discountCode;

import com.dazzle.asklepios.domain.DiscountCode;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.DiscountType;

import java.io.Serializable;
import java.math.BigDecimal;

public record DiscountCodeResponseVM(
        Long id,
        Long facilityId,
        String facilityName,
        String code,
        String name,
        String description,
        DiscountType discountType,
        BigDecimal percentage,
        BigDecimal fixedAmount,
        Currency currency,
        Boolean isActive
) implements Serializable {

    public static DiscountCodeResponseVM ofEntity(DiscountCode entity) {
        return new DiscountCodeResponseVM(
                entity.getId(),
                entity.getFacility() != null ? entity.getFacility().getId() : null,
                entity.getFacility() != null ? entity.getFacility().getName() : null,
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getDiscountType(),
                entity.getPercentage(),
                entity.getFixedAmount(),
                entity.getCurrency(),
                entity.getIsActive()
        );
    }
}
