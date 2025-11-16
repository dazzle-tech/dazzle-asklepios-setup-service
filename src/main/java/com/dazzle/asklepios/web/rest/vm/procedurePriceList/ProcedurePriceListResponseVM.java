package com.dazzle.asklepios.web.rest.vm.procedurePriceList;

import com.dazzle.asklepios.domain.ProcedurePriceList;

import java.io.Serializable;
import java.math.BigDecimal;

public record ProcedurePriceListResponseVM(
        Long id,
        Long procedureId,
        BigDecimal price,
        String currency
) implements Serializable {

    public static ProcedurePriceListResponseVM ofEntity(ProcedurePriceList entity) {
        return new ProcedurePriceListResponseVM(
                entity.getId(),
                entity.getProcedure() != null ? entity.getProcedure().getId() : null,
                entity.getPrice(),
                entity.getCurrency()
        );
    }
}
