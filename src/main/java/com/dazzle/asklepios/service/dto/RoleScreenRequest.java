package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import lombok.Data;

@Data
public class RoleScreenRequest {

    private Screen screen;
    private Operation permission;

    public RoleScreenRequest(Screen screen, Operation permission) {
        this.screen = screen;
        this.permission = permission;
    }
}
