package com.dazzle.asklepios.web.rest.vm;

import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** Body of a toggle-active call: the reason is required when deactivating and ignored when reactivating. */
public record ToggleActiveVM(
        @Size(max = 1000)
        String reason
) implements Serializable {

    public static String reasonOf(ToggleActiveVM vm) {
        return vm == null ? null : vm.reason();
    }
}
