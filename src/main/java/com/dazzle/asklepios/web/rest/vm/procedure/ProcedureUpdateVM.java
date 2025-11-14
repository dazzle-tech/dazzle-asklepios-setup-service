package com.dazzle.asklepios.web.rest.vm.procedure;

import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.enumeration.ProcedureCategoryType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * View Model for updating a Procedure via REST.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProcedureUpdateVM(
        @NotNull Long id,
        @NotEmpty String name,
        @NotEmpty String code,
        @NotNull ProcedureCategoryType categoryType,
        @NotNull Boolean isAppointable,
        String indications,
        String contraindications,
        String preparationInstructions,
        String recoveryNotes,
        @NotNull Boolean isActive,
        String lastModifiedBy
) implements Serializable {

    public static ProcedureUpdateVM ofEntity(Procedure procedure) {
        return new ProcedureUpdateVM(
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
                procedure.getLastModifiedBy());
    }
}
