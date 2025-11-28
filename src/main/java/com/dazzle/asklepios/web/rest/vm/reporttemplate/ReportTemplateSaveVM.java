package com.dazzle.asklepios.web.rest.vm.reporttemplate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record ReportTemplateSaveVM(
        Long id,  // nullable -> create, not-null -> update
        @NotBlank(message = "name cannot be null")
        String name,
        @NotBlank(message = "templateValue cannot be null")
        String templateValue,
        Boolean isActive
) implements Serializable {}
