package com.dazzle.asklepios.web.rest.Resource;

import com.dazzle.asklepios.domain.Resource;
import com.dazzle.asklepios.domain.enumeration.ResourceType;

import java.io.Serializable;

/**
 * View Model for reading a Resource via REST.
 */
public record ResourceResponseVM(
        Long id,
        ResourceType resourceType,
        String resourceKey,
        Boolean isAllowParallel,
        Boolean isActive
) implements Serializable {

    public static ResourceResponseVM ofEntity(Resource resource) {
        ResourceType resourceType;
        if (resource.getResourceType() != null && !resource.getResourceType().isBlank()) {
            try {
                resourceType = ResourceType.valueOf(resource.getResourceType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "Invalid ResourceType value: " + resource.getResourceType() + " for Resource id: " + resource.getId(),
                    e
                );
            }
        } else {
            throw new IllegalStateException("ResourceType cannot be null or blank for Resource id: " + resource.getId());
        }
        return new ResourceResponseVM(
                resource.getId(),
                resourceType,
                resource.getResourceKey(),
                resource.getIsAllowParallel(),
                resource.getIsActive()
        );
    }
}
