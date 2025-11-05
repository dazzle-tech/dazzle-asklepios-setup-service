package com.dazzle.asklepios.web.rest.vm.profile;

public record DiagnosticTestProfileResponseVM(
        Long id,
        Long testId,
        String name,
        String resultUnit
) {}
