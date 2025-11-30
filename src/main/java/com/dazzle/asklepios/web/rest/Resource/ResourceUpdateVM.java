package com.dazzle.asklepios.web.rest.Resource;

import com.dazzle.asklepios.domain.Resource;
import com.dazzle.asklepios.domain.enumeration.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record ResourceUpdateVM(
        @NotNull(message = "Resource type cannot be null")
        ResourceType resourceType,

        @NotBlank(message = "Resource key cannot be blank")
        @Size(max = 255, message = "Resource key cannot exceed 255 characters")
        String resourceKey,

        Boolean isAllowParallel,

        Boolean isActive
) implements Serializable {

    public static ResourceUpdateVM ofEntity(Resource resource) {
        return new ResourceUpdateVM(
                ResourceType.valueOf(resource.getResourceType()),
                resource.getResourceKey(),
                resource.getIsAllowParallel(),
                resource.getIsActive()
        );
    }
}
