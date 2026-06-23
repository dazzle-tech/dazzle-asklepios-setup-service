package com.dazzle.asklepios.service.dto.policyAssignment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
@JsonIgnoreProperties(ignoreUnknown = true)
public record PolicyAssignmentUpdateDTO(

        @NotNull Long id,

        @NotNull Boolean isRequired

) {
}
