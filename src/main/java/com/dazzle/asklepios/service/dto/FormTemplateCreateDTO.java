package com.dazzle.asklepios.service.dto;

public record FormTemplateCreateDTO(
        String name,
        String description,
        Long facilityId,
        Long departmentId,
        String formJson
) {}
