package com.dazzle.asklepios.web.rest.vm.procedure;

import com.dazzle.asklepios.domain.Procedure;

import java.io.Serializable;

public record ProcedureResponseVM(
        Long id,
        String name,
        String code,
        String categoryType,
        Boolean isAppointable,
        String indications,
        String contraindications,
        String preparationInstructions,
        String recoveryNotes,
        Boolean isActive,
        Long facilityId
) implements Serializable {

    public static ProcedureResponseVM ofEntity(Procedure procedure) {
        return new ProcedureResponseVM(
                procedure.getId(),
                procedure.getName(),
                procedure.getCode(),
                procedure.getCategoryType(),
                procedure.getIsAppointable(),
                procedure.getIndications(),
                procedure.getContraindications(),
                procedure.getPreparationInstructions(),
                procedure.getRecoveryNotes(),
                procedure.getIsActive(),
                procedure.getFacility() != null ? procedure.getFacility().getId() : null
        );
    }
}
