package com.dazzle.asklepios.web.rest.vm.profile;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DiagnosticTestProfileCreateVM(
        @NotNull Long testId,
        @NotBlank String name,
        String resultUnit
) {
    public DiagnosticTestProfile toEntity() {
        return DiagnosticTestProfile.builder()
                .test(DiagnosticTest.builder().id(testId).build())
                .name(name)
                .resultUnit(resultUnit)
                .build();
    }
}
