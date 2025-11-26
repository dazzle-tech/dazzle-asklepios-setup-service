package com.dazzle.asklepios.web.rest.vm.diagnosticTestCoding;

import com.dazzle.asklepios.domain.DiagnosticTestCoding;
import com.dazzle.asklepios.domain.enumeration.MedicalCodeType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record DiagnosticTestCodingUpdateVM(
        @NotNull Long id,
        @NotNull MedicalCodeType codeType,
        @NotEmpty String codeId,
        String lastModifiedBy
) implements Serializable {

    public static DiagnosticTestCodingUpdateVM ofEntity(DiagnosticTestCoding entity) {
        return new DiagnosticTestCodingUpdateVM(
                entity.getId(),
                entity.getCodeType(),
                entity.getCodeId(),
                entity.getLastModifiedBy()
        );
    }
}
