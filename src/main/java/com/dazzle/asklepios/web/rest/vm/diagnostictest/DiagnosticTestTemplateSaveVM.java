package com.dazzle.asklepios.web.rest.vm.diagnostictest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record DiagnosticTestTemplateSaveVM(
        @NotNull(message = "diagnosticTestId cannot be null")
        Long diagnosticTestId,

        @NotBlank(message = "name cannot be null")
        String name,

        @NotBlank(message = "templateValue cannot be null")
        String templateValue,

        Boolean isActive
) implements Serializable {}

