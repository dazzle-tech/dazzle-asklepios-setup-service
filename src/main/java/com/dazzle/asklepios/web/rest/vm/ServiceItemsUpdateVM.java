package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.ServiceItems;
import com.dazzle.asklepios.domain.enumeration.ServiceItemsType;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;

/**
 * View Model for updating a ServiceItems via REST.
 */
public record ServiceItemsUpdateVM(
        @NotNull Long id,
        @NotNull ServiceItemsType type,
        @NotNull Long sourceId,
        @NotNull Long serviceId,
        @NotNull Boolean isActive,
        String lastModifiedBy,
        Instant lastModifiedDate
) implements Serializable {

    /**
     * Convert entity -> ViewModel
     */
    public static ServiceItemsUpdateVM ofEntity(ServiceItems serviceItems) {
        return new ServiceItemsUpdateVM(
                serviceItems.getId(),
                serviceItems.getType(),
                serviceItems.getSourceId(),
                serviceItems.getService() != null ? serviceItems.getService().getId() : null,
                serviceItems.getIsActive(),
                serviceItems.getLastModifiedBy(),
                serviceItems.getLastModifiedDate()
        );
    }
}
