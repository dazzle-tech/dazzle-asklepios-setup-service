package com.dazzle.asklepios.web.rest.vm.reporttemplate;

import com.dazzle.asklepios.domain.ReportTemplate;

import java.io.Serializable;
import java.time.Instant;

public record ReportTemplateResponseVM(
        Long id,
        String name,
        String templateValue,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) implements Serializable {

    public static ReportTemplateResponseVM ofEntity(ReportTemplate t) {
        return new ReportTemplateResponseVM(
                t.getId(),
                t.getName(),
                t.getTemplateValue(),
                t.getIsActive(),
                t.getCreatedDate(),
                t.getLastModifiedDate()
        );
    }
}
