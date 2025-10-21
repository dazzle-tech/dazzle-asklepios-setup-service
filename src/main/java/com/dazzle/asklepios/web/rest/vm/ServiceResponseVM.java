package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import java.io.Serializable;
import java.math.BigDecimal;

public record ServiceResponseVM(
        Long id,
        String name,
        String abbreviation,
        String code,
        ServiceCategory category,
        BigDecimal price,
        Currency currency,
        Boolean isActive,
        Long facilityId
) implements Serializable {

    public static ServiceResponseVM ofEntity(ServiceSetup service) {
        return new ServiceResponseVM(
                service.getId(),
                service.getName(),
                service.getAbbreviation(),
                service.getCode(),
                service.getCategory(),
                service.getPrice(),
                service.getCurrency(),
                service.getIsActive(),
                service.getFacility() != null ? service.getFacility().getId() : null
        );
    }
}
