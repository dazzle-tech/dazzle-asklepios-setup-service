package com.dazzle.asklepios.web.rest.vm.profile;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DiagnosticTestProfileUpdateVM(
        @NotNull Long id,
        @NotNull Long testId,
        @NotBlank String name,
        String resultUnit,
        @NotNull TestResultType resultType,
        Boolean isDefault
) {
    public DiagnosticTestProfile toEntity() {
        return DiagnosticTestProfile.builder()
                .id(id)
                .test(DiagnosticTest.builder().id(testId).build())
                .name(name)
                .resultUnit(resultUnit)
                .resultType(resultType)
                .isDefault(isDefault != null ? isDefault : false)
                .build();
    }
}
