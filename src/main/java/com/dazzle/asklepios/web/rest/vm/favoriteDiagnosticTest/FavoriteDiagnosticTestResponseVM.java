package com.dazzle.asklepios.web.rest.vm.favoriteDiagnosticTest;

import com.dazzle.asklepios.domain.FavoriteDiagnosticTest;

public record FavoriteDiagnosticTestResponseVM(
        Long id,
        Long userId,
        Long testId
) {

    public static FavoriteDiagnosticTestResponseVM ofEntity(
            FavoriteDiagnosticTest entity
    ) {
        return new FavoriteDiagnosticTestResponseVM(
                entity.getId(),
                entity.getUserId(),
                entity.getTestId()
        );
    }
}
