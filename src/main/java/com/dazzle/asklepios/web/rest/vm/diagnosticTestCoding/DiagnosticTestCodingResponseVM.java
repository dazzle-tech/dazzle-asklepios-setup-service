package com.dazzle.asklepios.web.rest.vm.diagnosticTestCoding;

import com.dazzle.asklepios.domain.DiagnosticTestCoding;
import com.dazzle.asklepios.domain.enumeration.MedicalCodeType;

import java.io.Serializable;

public record DiagnosticTestCodingResponseVM(
        Long id,
        Long diagnosticTestId,
        MedicalCodeType codeType,
        String codeId
) implements Serializable {

    public static DiagnosticTestCodingResponseVM ofEntity(DiagnosticTestCoding entity) {
        return new DiagnosticTestCodingResponseVM(
                entity.getId(),
                entity.getDiagnosticTest() != null ? entity.getDiagnosticTest().getId() : null,
                entity.getCodeType(),
                entity.getCodeId()
        );
    }
}
