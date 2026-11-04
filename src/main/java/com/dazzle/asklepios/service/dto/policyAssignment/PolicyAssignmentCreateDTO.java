package com.dazzle.asklepios.service.dto.policyAssignment;

import com.dazzle.asklepios.domain.enumeration.PolicyResourceType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
@JsonIgnoreProperties(ignoreUnknown = true)
public record PolicyAssignmentCreateDTO(

        @NotNull Long policyId,

        @NotNull PolicyResourceType resourceType,

        @NotNull Long resourceId,

        @NotNull Boolean isRequired

) {
}