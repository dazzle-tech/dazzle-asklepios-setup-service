package com.dazzle.asklepios.web.rest.Resource;

import com.dazzle.asklepios.domain.Resource;
import com.dazzle.asklepios.domain.enumeration.ResourceType;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;

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
                throw new BadRequestAlertException(
                    "Invalid ResourceType value: " + resource.getResourceType() + " for Resource id: " + resource.getId(),
                    "resource",
                    "invalid.resourcetype"
                );
            }
        } else {
            throw new BadRequestAlertException(
                "ResourceType cannot be null or blank for Resource id: " + resource.getId(),
                "resource",
                "missing.resourcetype"
            );
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
