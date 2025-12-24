package com.dazzle.asklepios.web.rest.vm;

import java.time.Instant;

public record FormEntryResponseVM(
        Long id,
        String title,
        Long templateId,
        String templateName,
        Long facilityId,
        Long departmentId,
        String dataJson,
        Instant createdDate,
        String createdBy,
        Instant lastModifiedDate,
        String lastModifiedBy
) {}
