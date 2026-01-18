package com.dazzle.asklepios.service.dto;

public record FormEntryCreateDTO(
        String title,
        Long templateId,
        Long facilityId,
        Long departmentId,
        String dataJson
) {}
