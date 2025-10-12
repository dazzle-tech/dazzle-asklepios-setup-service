package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Service;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record ServiceCreateVM(
        @NotNull String name,
        String abbreviation,
        @NotNull String code,
        ServiceCategory category,
        BigDecimal price,
        @NotNull Currency currency,
        @NotNull Boolean isActive,
        @NotNull String createdBy
) implements Serializable {

    public static ServiceCreateVM ofEntity(Service service) {
        return new ServiceCreateVM(
                service.getName(),
                service.getAbbreviation(),
                service.getCode(),
                service.getCategory(),
                service.getPrice(),
                service.getCurrency(),
                service.getIsActive(),
                service.getCreatedBy()
        );
    }
}
