package com.dazzle.asklepios.web.rest.vm.procedure;

import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ProcedureCategory;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public record ProcedureResponseVM(
        Long id,
        String name,
        String code,
        ProcedureCategory categoryType,
        Boolean isAppointable,
        String indications,
        String contraindications,
        String preparationInstructions,
        String recoveryNotes,
        Boolean isActive,
        Long facilityId,
       Currency currency,
       BigDecimal price,
       Long billingRuleId,
        List<String> relatedSurfaces
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
                procedure.getFacility() != null ? procedure.getFacility().getId() : null,
                procedure.getCurrency(),
                procedure.getPrice(),
                procedure.getBillingRule() != null
                        ? procedure.getBillingRule().getId()
                        : null,
                parseSurfaces(procedure.getRelatedSurfaces())
        );
    }

    private static List<String> parseSurfaces(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .toList();
    }
}
