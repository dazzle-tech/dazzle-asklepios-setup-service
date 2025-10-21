package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.ServiceItems;
import com.dazzle.asklepios.domain.enumeration.ServiceItemsType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ServiceItemsCreateVM(
        @NotNull ServiceItemsType type,
        @NotNull Long sourceId,
        @NotNull Long serviceId,
        String createdBy,
        Boolean isActive,
        boolean b) implements Serializable {

    /**
     * Convert entity -> ViewModel (for reading from DB)
     */
    public static ServiceItemsCreateVM ofEntity(ServiceItems serviceItems) {
        return new ServiceItemsCreateVM(
                serviceItems.getType(),
                serviceItems.getSourceId(),
                serviceItems.getService() != null ? serviceItems.getService().getId() : null,
                serviceItems.getCreatedBy(),
                serviceItems.getIsActive(),
                true);
    }

}
