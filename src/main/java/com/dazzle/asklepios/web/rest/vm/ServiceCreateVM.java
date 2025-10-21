package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ServiceCreateVM(
        @NotNull String name,
        String abbreviation,
        @NotNull String code,
        @NotNull ServiceCategory category,
        BigDecimal price,
        @NotNull Currency currency,
        Boolean isActive,
        String createdBy,
        @NotNull Long facilityId
) implements Serializable {

    public static ServiceCreateVM ofEntity(ServiceSetup service) {
        return new ServiceCreateVM(
                service.getName(),
                service.getAbbreviation(),
                service.getCode(),
                service.getCategory(),
                service.getPrice(),
                service.getCurrency(),
                service.getIsActive(),
                service.getCreatedBy(),
                service.getFacility() != null ? service.getFacility().getId() : null
        );
    }
}
