package com.dazzle.asklepios.web.rest.vm.diagnosticTestCoding;

import com.dazzle.asklepios.domain.DiagnosticTestCoding;
import com.dazzle.asklepios.domain.enumeration.MedicalCodeType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DiagnosticTestCodingCreateVM(
        @NotNull MedicalCodeType codeType,
        @NotEmpty String codeId
) implements Serializable {

    public static DiagnosticTestCodingCreateVM ofEntity(DiagnosticTestCoding entity) {
        return new DiagnosticTestCodingCreateVM(
                entity.getCodeType(),
                entity.getCodeId()
        );
    }
}
