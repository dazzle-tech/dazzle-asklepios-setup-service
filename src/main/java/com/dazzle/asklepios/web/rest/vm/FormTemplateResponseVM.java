package com.dazzle.asklepios.web.rest.vm;

import java.time.Instant;

public record FormTemplateResponseVM(
        Long id,
        String name,
        String description,
        Long facilityId,
        String facilityName,
        Long departmentId,
        String departmentName,
        String formJson,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate
) {}

