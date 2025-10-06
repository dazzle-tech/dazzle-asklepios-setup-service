package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;

public record RoleScreenVM(
        Screen screen,
        Operation permission
) {}

