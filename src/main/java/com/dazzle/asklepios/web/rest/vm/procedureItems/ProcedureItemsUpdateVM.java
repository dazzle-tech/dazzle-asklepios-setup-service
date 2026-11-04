package com.dazzle.asklepios.web.rest.vm.procedureItems;

import com.dazzle.asklepios.domain.ProcedureItems;
import com.dazzle.asklepios.domain.enumeration.ProcedureItemsType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProcedureItemsUpdateVM(
        @NotNull Long id,
        @NotNull ProcedureItemsType type,
        @NotNull Long sourceId,
        @NotNull Long procedureId,
        String specialty,
        @NotNull Boolean isActive
) implements Serializable {

    public ProcedureItems toEntity() {
        return ProcedureItems.builder()
                .type(type)
                .sourceId(sourceId)
                .specialty(specialty)
                .isActive(isActive)
                .build();
    }
}
