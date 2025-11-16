package com.dazzle.asklepios.web.rest.vm.procedurePriceList;

import com.dazzle.asklepios.domain.ProcedurePriceList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProcedurePriceListCreateVM(
        @NotNull @PositiveOrZero BigDecimal price,
        @NotNull String currency
) implements Serializable {

    public static ProcedurePriceListCreateVM ofEntity(ProcedurePriceList entity) {
        return new ProcedurePriceListCreateVM(
                entity.getPrice(),
                entity.getCurrency()
        );
    }
}
