package com.dazzle.asklepios.service.dto;

public record FormTemplateUpdateDTO(
        String name,
        String description,
        Long facilityId,
        Long departmentId,
        String formJson
) {}
