package com.dazzle.asklepios.web.rest.vm.profile;

import jakarta.validation.constraints.NotNull;

public record DiagnosticTestProfileUpdateVM(
        @NotNull Long id,
        @NotNull Long testId,
        @NotNull String name,
        String resultUnit
) {}
