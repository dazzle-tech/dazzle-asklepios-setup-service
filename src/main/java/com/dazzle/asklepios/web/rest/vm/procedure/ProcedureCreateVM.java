package com.dazzle.asklepios.web.rest.vm.procedure;

import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.enumeration.ProcedureCategoryType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProcedureCreateVM(
        @NotEmpty String name,
        @NotEmpty String code,
        @NotNull ProcedureCategoryType categoryType,
        @NotNull Boolean isAppointable,
        String indications,
        String contraindications,
        String preparationInstructions,
        String recoveryNotes,
        @NotNull Boolean isActive
) implements Serializable {

    public static ProcedureCreateVM ofEntity(Procedure procedure) {
        return new ProcedureCreateVM(
                procedure.getName(),
                procedure.getCode(),
                procedure.getCategoryType(),
                procedure.getIsAppointable(),
                procedure.getIndications(),
                procedure.getContraindications(),
                procedure.getPreparationInstructions(),
                procedure.getRecoveryNotes(),
                procedure.getIsActive()
        );
    }
}
