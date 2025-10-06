package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import jakarta.validation.constraints.NotNull;

public record RoleScreenVM(
        @NotNull Screen screen,
        @NotNull Operation permission
) {}
