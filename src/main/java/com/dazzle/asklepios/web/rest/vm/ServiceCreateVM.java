package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ServiceCreateVM(
        @NotEmpty String name,
        String abbreviation,
        @NotEmpty String code,
        @NotNull ServiceCategory category,
        BigDecimal price,
        @NotNull Currency currency,
        Boolean isActive,
        Boolean appointable,
        Integer parallelCapacityValue,
        Integer defaultDurationMinutes,
        Integer defaultBufferBeforeMinutes,
        Integer defaultBufferAfterMinutes,
        Long billingRuleId
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
                service.getAppointable(),
                service.getParallelCapacityValue(),
                service.getDefaultDurationMinutes(),
                service.getDefaultBufferBeforeMinutes(),
                service.getDefaultBufferAfterMinutes(),
                service.getBillingRule() != null
                        ? service.getBillingRule().getId()
                        : null
        );
    }
}
