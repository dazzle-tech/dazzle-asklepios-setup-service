package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * View Model for updating a Service via REST.
 */
public record ServiceUpdateVM(
        @NotNull Long id,
        @NotNull String name,
        String abbreviation,
        @NotNull String code,
        ServiceCategory category,
        BigDecimal price,
        @NotNull Currency currency,
        @NotNull Boolean isActive,
        String lastModifiedBy,
        @NotNull Long facilityId
) implements Serializable {

    public static ServiceUpdateVM ofEntity(ServiceSetup service) {
        return new ServiceUpdateVM(
                service.getId(),
                service.getName(),
                service.getAbbreviation(),
                service.getCode(),
                service.getCategory(),
                service.getPrice(),
                service.getCurrency(),
                service.getIsActive(),
                service.getLastModifiedBy(),
                service.getFacility() != null ? service.getFacility().getId() : null
        );
    }
}
