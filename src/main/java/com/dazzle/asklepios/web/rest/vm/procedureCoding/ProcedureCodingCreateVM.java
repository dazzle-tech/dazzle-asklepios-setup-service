package com.dazzle.asklepios.web.rest.vm.procedureCoding;

import com.dazzle.asklepios.domain.ProcedureCoding;
import com.dazzle.asklepios.domain.enumeration.MedicalCodeType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProcedureCodingCreateVM(
        @NotNull MedicalCodeType codeType,
        @NotEmpty String codeId
) implements Serializable {

    public static ProcedureCodingCreateVM ofEntity(ProcedureCoding entity) {
        return new ProcedureCodingCreateVM(
                entity.getCodeType(),
                entity.getCodeId()
        );
    }
}
