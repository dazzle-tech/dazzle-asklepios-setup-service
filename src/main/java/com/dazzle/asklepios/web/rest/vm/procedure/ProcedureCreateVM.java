package com.dazzle.asklepios.web.rest.vm.procedure;

import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ProcedureCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProcedureCreateVM(
        @NotEmpty String name,
        @NotEmpty String code,
        @NotNull ProcedureCategory categoryType,
        @NotNull Boolean isAppointable,
        String indications,
        String contraindications,
        String preparationInstructions,
        String recoveryNotes,
       @NotNull Currency currency,
        @NotNull Long price,
        @NotNull Boolean isActive,
        Long billingRuleId
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
                procedure.getCurrency(),
                procedure.getPrice(),
                procedure.getIsActive(),
                procedure.getBillingRule() != null
                        ? procedure.getBillingRule().getId()
                        : null
        );
    }
}
