// src/main/java/com/dazzle/asklepios/web/rest/vm/MenuItemVM.java
package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;

import java.util.EnumSet;

public record MenuItemVM(
        Screen screen,                // backend enum value
        EnumSet<Operation> operations // union of ops from all roles
) {
    public static MenuItemVM of(Screen screen, EnumSet<Operation> ops) {
        return new MenuItemVM(screen, ops == null ? EnumSet.noneOf(Operation.class) : ops);
    }
}
