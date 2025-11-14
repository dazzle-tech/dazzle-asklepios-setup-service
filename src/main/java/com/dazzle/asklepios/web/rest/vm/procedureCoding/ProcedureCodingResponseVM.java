package com.dazzle.asklepios.web.rest.vm.procedureCoding;

import com.dazzle.asklepios.domain.ProcedureCoding;
import com.dazzle.asklepios.domain.enumeration.MedicalCodeType;

import java.io.Serializable;

public record ProcedureCodingResponseVM(
        Long id,
        Long procedureId,
        MedicalCodeType codeType,
        String codeId
) implements Serializable {

    public static ProcedureCodingResponseVM ofEntity(ProcedureCoding entity) {
        return new ProcedureCodingResponseVM(
                entity.getId(),
                entity.getProcedure() != null ? entity.getProcedure().getId() : null,
                entity.getCodeType(),
                entity.getCodeId()
        );
    }
}
