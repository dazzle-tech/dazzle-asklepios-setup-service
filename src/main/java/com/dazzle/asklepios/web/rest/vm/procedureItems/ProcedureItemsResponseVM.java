package com.dazzle.asklepios.web.rest.vm.procedureItems;

import com.dazzle.asklepios.domain.ProcedureItems;
import com.dazzle.asklepios.domain.enumeration.ProcedureItemsType;

import java.io.Serializable;
import java.time.Instant;

public record ProcedureItemsResponseVM(
        Long id,
        ProcedureItemsType type,
        Long sourceId,
        String specialty,
        Long procedureId,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate,
        Boolean isActive
) implements Serializable {

    public static ProcedureItemsResponseVM ofEntity(ProcedureItems procedureItems) {
        return new ProcedureItemsResponseVM(
                procedureItems.getId(),
                procedureItems.getType(),
                procedureItems.getSourceId(),
                procedureItems.getSpecialty(),
                procedureItems.getProcedure() != null ? procedureItems.getProcedure().getId() : null,
                procedureItems.getCreatedBy(),
                procedureItems.getCreatedDate(),
                procedureItems.getLastModifiedBy(),
                procedureItems.getLastModifiedDate(),
                procedureItems.getIsActive()
        );
    }
}
