package com.dazzle.asklepios.web.rest.vm.profile;

import com.dazzle.asklepios.domain.DiagnosticTestProfile;

public record DiagnosticTestProfileResponseVM(
        Long id,
        Long testId,
        String name,
        String resultUnit
) {
    public static DiagnosticTestProfileResponseVM fromEntity(DiagnosticTestProfile e) {
        return new DiagnosticTestProfileResponseVM(
                e.getId(),
                e.getTest() != null ? e.getTest().getId() : null,
                e.getName(),
                e.getResultUnit()
        );
    }
}
