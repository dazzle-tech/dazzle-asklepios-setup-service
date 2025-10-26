package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.ServiceItems;
import com.dazzle.asklepios.domain.enumeration.ServiceItemsType;
import java.io.Serializable;
import java.time.Instant;

/**
 * View Model for returning ServiceItems data via REST responses.
 */
public record ServiceItemsResponseVM(
        Long id,
        ServiceItemsType type,
        Long sourceId,
        Long serviceId,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate,
        Boolean isActive
) implements Serializable {

    /**
     * Convert entity -> Response ViewModel
     */
    public static ServiceItemsResponseVM ofEntity(ServiceItems serviceItems) {
        return new ServiceItemsResponseVM(
                serviceItems.getId(),
                serviceItems.getType(),
                serviceItems.getSourceId(),
                serviceItems.getService() != null ? serviceItems.getService().getId() : null,
                serviceItems.getCreatedBy(),
                serviceItems.getCreatedDate(),
                serviceItems.getLastModifiedBy(),
                serviceItems.getLastModifiedDate(),
                serviceItems.getIsActive()
        );
    }
}
