package com.dazzle.asklepios.web.rest.vm.profile;

import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.enumeration.TestResultType;

public record DiagnosticTestProfileResponseVM(
        Long id,
        Long testId,
        String name,
        String resultUnit,
        TestResultType resultType,
        Boolean isDefault,
        Boolean isActive
) {
    public static DiagnosticTestProfileResponseVM fromEntity(DiagnosticTestProfile e) {
        return new DiagnosticTestProfileResponseVM(
                e.getId(),
                e.getTest() != null ? e.getTest().getId() : null,
                e.getName(),
                e.getResultUnit(),
                e.getResultType(),
                e.getIsDefault(),
                e.getIsActive()
        );
    }
}
