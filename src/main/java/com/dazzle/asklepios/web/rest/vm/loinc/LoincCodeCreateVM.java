package com.dazzle.asklepios.web.rest.vm.loinc;

import com.dazzle.asklepios.domain.enumeration.LoincCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoincCodeCreateVM(
        @NotBlank @Size(max = 20) String code,
        @NotNull LoincCategory category,
        @NotBlank @Size(max = 500) String description
) implements Serializable {}
