package com.dazzle.asklepios.web.rest.vm.diagnostictest;


import com.dazzle.asklepios.domain.DiagnosticTestReportTemplate;

import java.io.Serializable;
import java.time.Instant;

public record DiagnosticTestTemplateResponseVM(
        Long id,
        Long diagnosticTestId,
        String name,
        String templateValue,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) implements Serializable {

    public static DiagnosticTestTemplateResponseVM ofEntity(DiagnosticTestReportTemplate t) {
        return new DiagnosticTestTemplateResponseVM(
                t.getId(),
                t.getDiagnosticTest().getId(),
                t.getName(),
                t.getTemplateValue(),
                t.getIsActive(),
                t.getCreatedDate(),
                t.getLastModifiedDate()
        );
    }
}
