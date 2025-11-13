package com.dazzle.asklepios.web.rest.vm.profile;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import jakarta.validation.constraints.NotNull;

public record DiagnosticTestProfileUpdateVM(
        @NotNull Long id,
        @NotNull Long testId,
        @NotNull String name,
        String resultUnit
) {
    public DiagnosticTestProfile toEntity() {
        return DiagnosticTestProfile.builder()
                .id(id)
                .test(DiagnosticTest.builder().id(testId).build())
                .name(name)
                .resultUnit(resultUnit)
                .build();
    }
}
