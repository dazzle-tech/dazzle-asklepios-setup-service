package com.dazzle.asklepios.web.rest.vm.procedureCoding;

import com.dazzle.asklepios.domain.ProcedureCoding;
import com.dazzle.asklepios.domain.enumeration.MedicalCodeType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record ProcedureCodingUpdateVM(
        @NotNull Long id,
        @NotNull MedicalCodeType codeType,
        @NotEmpty String codeId,
        String lastModifiedBy
) implements Serializable {

    public static ProcedureCodingUpdateVM ofEntity(ProcedureCoding entity) {
        return new ProcedureCodingUpdateVM(
                entity.getId(),
                entity.getCodeType(),
                entity.getCodeId(),
                entity.getLastModifiedBy()
        );
    }
}
