package com.dazzle.asklepios.service.dto.FavoriteDiagnosticsTests;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record FavoriteDiagnosticTestCreateDTO(

        @NotNull
        Long userId,

        @NotNull
        Long testId

) implements Serializable {
}
