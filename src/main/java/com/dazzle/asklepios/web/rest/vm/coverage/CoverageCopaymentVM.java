package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.CoverageCopayment;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;

import java.math.BigDecimal;
import java.time.Instant;

public record CoverageCopaymentVM(
        Long id,
        EncounterType encounterType,
        InsuranceCoverageType valueType,
        BigDecimal valueAmount,
        Boolean discountOnExcluded,
        Boolean discountOnCash,
        Boolean discountOnExceededCash,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {
    public static CoverageCopaymentVM ofEntity(CoverageCopayment entity) {
        return new CoverageCopaymentVM(
                entity.getId(),
                entity.getEncounterType(),
                entity.getValueType(),
                entity.getValueAmount(),
                entity.getDiscountOnExcluded(),
                entity.getDiscountOnCash(),
                entity.getDiscountOnExceededCash(),
                entity.getIsActive(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate()
        );
    }
}
