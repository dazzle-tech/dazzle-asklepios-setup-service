package com.dazzle.asklepios.web.rest.vm.coverage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record CoverageClassUpdateVM(
        @NotNull
        Long id,
        @NotBlank
        @Size(max = 100)
        String name,
        Boolean isActive
) implements Serializable {}
